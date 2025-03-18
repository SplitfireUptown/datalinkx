package com.datalinkx.dataserver.service.impl;


import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_STOP;
import static com.datalinkx.common.utils.IdUtils.genKey;
import static com.datalinkx.common.utils.JsonUtils.toJson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.domain.JobLogBean;
import com.datalinkx.dataserver.bean.dto.JobDto;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.client.xxljob.JobClientApi;
import com.datalinkx.dataserver.client.xxljob.request.XxlJobParam;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.repository.DsRepository;
import com.datalinkx.dataserver.repository.JobLogRepository;
import com.datalinkx.dataserver.repository.JobRelationRepository;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.JobService;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;



@Service
@Log4j2
public class JobServiceImpl implements JobService {

	@Autowired
	JobRepository jobRepository;

	@Autowired
	JobRelationRepository jobRelationRepository;

	@Autowired
	DsServiceImpl dsServiceImpl;

	@Autowired
	DsRepository dsRepository;

	@Autowired
	JobLogRepository jobLogRepository;

	@Autowired
	JobClientApi jobClientApi;


	@Transactional(rollbackFor = Exception.class)
	public String jobCreate(JobForm.JobCreateForm form) {
		this.validJobForm(form);
		String jobId = genKey("job");
		JobBean jobBean = new JobBean();
		jobBean.setJobId(jobId);

		jobBean.setReaderDsId(form.getFromDsId());
		jobBean.setWriterDsId(form.getToDsId());

		jobBean.setConfig(toJson(form.getFieldMappings()));
		jobBean.setFromTbId(form.getFromTbName());
		jobBean.setToTbId(form.getToTbName());
		jobBean.setStatus(MetaConstants.JobStatus.JOB_STATUS_CREATE);
		jobBean.setCrontab(form.getSchedulerConf());
		jobBean.setSyncMode(JsonUtils.toJson(form.getSyncMode()));
		jobBean.setName(form.getJobName());
		jobBean.setCover(form.getCover());
		jobBean.setGraph(form.getGraph());
		jobBean.setType(form.getType());

		// 创建 xxljob
		String xxlJobId = jobClientApi.add(form.getSchedulerConf(), XxlJobParam.builder().jobId(jobId).build());

		jobBean.setXxlId(xxlJobId);
		jobRepository.save(jobBean);
		return jobId;
	}


	public String jobModify(JobForm.JobModifyForm form) {
		this.validJobForm(form);
		JobBean jobBean = jobRepository.findByJobId(form.getJobId()).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
		jobBean.setReaderDsId(form.getFromDsId());
		jobBean.setWriterDsId(form.getToDsId());
		jobBean.setConfig(toJson(form.getFieldMappings()));
		jobBean.setFromTbId(form.getFromTbName());
		jobBean.setToTbId(form.getToTbName());
		jobBean.setCrontab(form.getSchedulerConf());
		jobBean.setSyncMode(JsonUtils.toJson(form.getSyncMode()));
		jobBean.setName(form.getJobName());
		jobBean.setCover(form.getCover());
		jobBean.setGraph(form.getGraph());
		jobRepository.save(jobBean);
		return form.getJobId();
	}

	// 校验计算任务transform graph是否合法
	private void validTransformGraph(DsBean fromDsBean, JobForm.JobCreateForm form) {
		if (ObjectUtils.isEmpty(form.getGraph())) {
			return;
		}

		Map<String, Integer> nodeBook = new HashMap<>();
		JsonNode jsonNode = JsonUtils.toJsonNode(form.getGraph());
		for (JsonNode node : jsonNode.get("cells")) {
			String nodeType = node.get("shape").asText();
			if ("edge".equals(nodeType)) {
				continue;
			}

			nodeBook.put(nodeType, nodeBook.getOrDefault(nodeType, 0) + 1);
		}

		// 目前只支持画布中三个节点, 前端太难写了，GPT都救不了，实在写不下去了。。。。
		if (nodeBook.keySet().size() > 3) {
			throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "仅支持单source输入节点、单输出sink节点、单transform节点");
		}

		// 目前只支持单source输入节点、单输出sink节点、单transform节点
		List<Integer> normalNodeNum = nodeBook.values().stream().filter(v -> v > 1).collect(Collectors.toList());
		if (!ObjectUtils.isEmpty(normalNodeNum)) {
			throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "仅支持单source输入节点、单输出sink节点、单transform节点");
		}

		// 如果来源数据源是http数据源，SQL算子无意义
		if (MetaConstants.DsType.HTTP.equals(fromDsBean.getType()) && nodeBook.containsKey("sql")) {
			throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "HTTP数据源不支持SQL算子");
		}
	}

	// 校验流转任务配置合法
	public void validJobForm(JobForm.JobCreateForm form) {
		// 1、判断数据源是否存在
		DsBean fromDsBean = dsRepository.findByDsId(form.getFromDsId()).orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "来源数据源不存在"));
		dsRepository.findByDsId(form.getToDsId()).orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "目标数据源不存在"));
		// 2、判断流转任务名称是否重复
		jobRepository.findByName(form.getJobName()).ifPresent(jobBean -> {
			if (form instanceof JobForm.JobModifyForm) {
				if (!ObjectUtils.nullSafeEquals(jobBean.getJobId(), ((JobForm.JobModifyForm) form).getJobId())) {
					throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "任务名称已存在");
				}
			} else {
				throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "任务名称已存在");
			}
		});

		// 3、判断增量模式下是否有增量字段
		if ((!ObjectUtils.isEmpty(form.getSyncMode()) && MetaConstants.JobSyncMode.INCREMENT_MODE.equals(form.getSyncMode().getMode()))
				&& ObjectUtils.isEmpty(form.getSyncMode().getIncreateField())) {
			throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "增量模式必须指定增量字段");
		}
		// 4、判断增量模式下是否是时间类型或数值类型
		if ((!ObjectUtils.isEmpty(form.getSyncMode()) && MetaConstants.JobSyncMode.INCREMENT_MODE.equals(form.getSyncMode().getMode()))) {
			try {
				IDsReader dsReader = DsDriverFactory.getDsReader(dsServiceImpl.getConnectId(fromDsBean));
				Boolean isIncremental = dsReader.judgeIncrementalField(fromDsBean.getDatabase(), fromDsBean.getSchema(), form.getFromTbName(), form.getSyncMode().getIncreateField());
				if (!isIncremental) {
					throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "增量字段必须是日期或数值类型");
				}
			} catch (Exception e) {
				throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, e.getMessage());
			}
		}
		// 5、配置流转任务定时表达式
		if (ObjectUtils.isEmpty(form.getSchedulerConf()) && !ObjectUtils.nullSafeEquals(form.getType(), MetaConstants.JobType.JOB_TYPE_STREAM)) {
			throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "批式流转任务需要配置crontab表达式");
		}
		// 6、校验计算任务transform graph是否合法
		this.validTransformGraph(fromDsBean, form);
	}


	// 创建任务后再手动调用xxl-job触发函数，解决异步调用mysql事务未提交，xxl-job任务已出发问题
	public String jobCreateProxy(JobForm.JobCreateForm form) {
		String jobId = this.jobCreate(form);
		// 1、创建后开启任务
		jobClientApi.start(jobId);
		// 2、默认触发一次任务
		jobClientApi.trigger(jobId, XxlJobParam.builder().jobId(jobId).build());
		return jobId;
	}

	public JobVo.JobInfoVo info(String jobId) {
		JobBean jobBean = jobRepository.findByJobId(jobId)
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));


		JobVo.JobInfoVo jobInfoVo = JobVo.JobInfoVo
				.builder()
				.jobId(jobBean.getJobId())
				.jobName(jobBean.getName())
				.fromDsId(jobBean.getReaderDsId())
				.toDsId(jobBean.getWriterDsId())
				.fromTbName(jobBean.getFromTbId())
				.toTbName(jobBean.getToTbId())
				.schedulerConf(jobBean.getCrontab())
				.cover(jobBean.getCover())
				.graph(jobBean.getGraph())
				.syncMode(JsonUtils.toObject(jobBean.getSyncMode(), JobForm.SyncModeForm.class))
				.build();

		List<JobForm.FieldMappingForm> fieldMappingForms = JsonUtils.toList(jobBean.getConfig(), JobForm.FieldMappingForm.class);
		jobInfoVo.setFieldMappings(fieldMappingForms);
		return jobInfoVo;
	}

	public PageVo<List<JobVo.JobPageVo>> page(JobForm.JobPageForm form) {
		PageRequest pageRequest = PageRequest.of(form.getPageNo() - 1, form.getPageSize());
		Page<JobBean> jobBeans = jobRepository.pageQuery(pageRequest, form.getType());


		List<String> dsId = new ArrayList<>();
		jobBeans.getContent().forEach(job -> {
			dsId.add(job.getWriterDsId());
			dsId.add(job.getReaderDsId());
		});


		Map<String, String> dsNameMap = dsRepository.findAllByDsIdIn(dsId)
				.stream()
				.collect(Collectors.toMap(DsBean::getDsId, DsBean::getName));

		List<JobVo.JobPageVo> pageVoList = jobBeans.getContent().stream().map(jobBean -> {
			JobDto.DataCountDto dataCountDto = JsonUtils.toObject(jobBean.getCount(), JobDto.DataCountDto.class);
			return JobVo.JobPageVo
					.builder()
					.jobId(jobBean.getJobId())
					.jobName(jobBean.getName())
					.progress(String.format("%s/%s", dataCountDto.getAppendCount(), dataCountDto.getFilterCount()))
					.fromTbName(dsNameMap.get(jobBean.getReaderDsId()) + "." + jobBean.getFromTbId())
					.toTbName(dsNameMap.get(jobBean.getWriterDsId()) + "."  + jobBean.getToTbId())
					.status(jobBean.getStatus())
					.build();
		}).collect(Collectors.toList());

		PageVo<List<JobVo.JobPageVo>> result = new PageVo<>();
		result.setPageNo(form.getPageNo());
		result.setPageSize(form.getPageSize());
		result.setData(pageVoList);
		result.setTotalPage(jobBeans.getTotalPages());
		result.setTotal(jobBeans.getTotalElements());
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	public void del(String jobId) {
		this.jobLogRepository.logicDeleteByJobId(jobId);
		this.jobClientApi.del(jobId);
		this.jobRepository.logicDeleteByJobId(jobId);
	}

	public PageVo<List<JobVo.JobLogPageVo>> logPage(JobForm.JobLogPageForm jobLogPageForm) {
		PageRequest pageRequest = PageRequest.of(jobLogPageForm.getPageNo() - 1, jobLogPageForm.getPageSize());
		Page<JobLogBean> jobLogBeans = jobLogRepository.pageQuery(pageRequest, jobLogPageForm.getJobId());

		List<JobVo.JobLogPageVo> logPageVos = jobLogBeans.getContent().stream().map(jobLogBean -> {
			JobVo.JobLogPageVo logPageVo = new JobVo.JobLogPageVo();
			logPageVo.setJobId(jobLogBean.getJobId());
			logPageVo.setStatus(jobLogBean.getStatus());
			logPageVo.setErrorMsg(jobLogBean.getErrorMsg());
			logPageVo.setCostTime(jobLogBean.getCostTime());
			logPageVo.setEndTime(jobLogBean.getEndTime());
			logPageVo.setStartTime(jobLogBean.getStartTime());


			JobDto.DataCountDto dataCountDto = JsonUtils.toObject(jobLogBean.getCount(), JobDto.DataCountDto.class);
			logPageVo.setAppendCount(dataCountDto.getAppendCount());
			return logPageVo;
		}).collect(Collectors.toList());

		PageVo<List<JobVo.JobLogPageVo>> result = new PageVo<>();
		result.setPageNo(jobLogPageForm.getPageNo());
		result.setPageSize(jobLogPageForm.getPageSize());
		result.setData(logPageVos);
		result.setTotalPage(jobLogBeans.getTotalPages());
		result.setTotal(jobLogBeans.getTotalElements());
		return result;
	}

	@Transactional(rollbackFor = Exception.class)
	public void jobStop(String jobId) {
		jobRepository.updateJobStatus(jobId, JOB_STATUS_STOP);
		jobClientApi.stop(jobId);
	}

	public List<JobVo.JobId2NameVo> list() {
		return jobRepository.findAll().stream().map(v -> JobVo.JobId2NameVo.builder().JobId(v.getJobId()).jobName(v.getName()).type(v.getType()).build()).collect(Collectors.toList());
	}
}