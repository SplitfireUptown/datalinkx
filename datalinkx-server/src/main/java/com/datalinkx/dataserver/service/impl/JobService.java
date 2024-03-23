package com.datalinkx.dataserver.service.impl;


import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_STOP;
import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SYNC;
import static com.datalinkx.common.utils.IdUtils.genKey;
import static com.datalinkx.common.utils.JsonUtils.toJson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.datalinkx.common.constants.MessageHubConstants;
import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.domain.DsTbBean;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.domain.JobLogBean;
import com.datalinkx.dataserver.bean.domain.JobRelationBean;
import com.datalinkx.dataserver.bean.dto.JobDto;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.client.xxljob.JobClientApi;
import com.datalinkx.dataserver.client.xxljob.request.DataTransJobParam;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.controller.form.JobStateForm;
import com.datalinkx.dataserver.repository.DsRepository;
import com.datalinkx.dataserver.repository.DsTbRepository;
import com.datalinkx.dataserver.repository.JobLogRepository;
import com.datalinkx.dataserver.repository.JobRelationRepository;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.DtsJobService;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.base.model.TableField;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.messagehub.bean.form.ProducerAdapterForm;
import com.datalinkx.messagehub.service.MessageHubService;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;



@Component
@Service
@Log4j2
public class JobService implements DtsJobService {
	private static final int FAILED = 3;
	private static final int SUCCESS = 2;

	@Autowired
	JobRepository jobRepository;

	@Autowired
	JobRelationRepository jobRelationRepository;

	@Autowired
	DsService dsService;

	@Autowired
	DsRepository dsRepository;

	@Autowired
	DsTbRepository dsTbRepository;

	@Autowired
	JobLogRepository jobLogRepository;

	@Autowired
	JobClientApi jobClientApi;

	@Resource(name = "messageHubServiceImpl")
	MessageHubService messageHubService;

	@Transactional(rollbackFor = Exception.class)
	public String jobCreate(JobForm.JobCreateForm form) {
		this.validJobForm(form);
		String jobId = genKey("job");
		JobBean jobBean = new JobBean();
		jobBean.setJobId(jobId);

		jobBean.setReaderDsId(form.getFromDsId());
		jobBean.setWriterDsId(form.getToDsId());

		jobBean.setConfig(toJson(form.getFieldMappings()));
		jobBean.setFromTbId(getXtbId(form.getFromTbName(), form.getFromDsId()));
		jobBean.setToTbId(getXtbId(form.getToTbName(), form.getFromDsId()));
		jobBean.setStatus(MetaConstants.JobStatus.JOB_STATUS_CREATE);
		jobBean.setCrontab(form.getSchedulerConf());
		jobBean.setSyncMode(JsonUtils.toJson(form.getSyncMode()));
		jobBean.setName(form.getJobName());
		jobBean.setCover(form.getCover());

		// 创建 xxljob
		String xxlJobId = jobClientApi.add(jobId, form.getSchedulerConf(), DataTransJobParam.builder().jobId(jobId).build());

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
		jobBean.setFromTbId(getXtbId(form.getFromTbName(), form.getFromDsId()));
		jobBean.setToTbId(getXtbId(form.getToTbName(), form.getFromDsId()));
		jobBean.setCrontab(form.getSchedulerConf());
		jobBean.setSyncMode(JsonUtils.toJson(form.getSyncMode()));
		jobBean.setName(form.getJobName());
		jobBean.setCover(form.getCover());
		jobRepository.save(jobBean);
		return form.getJobId();
	}

	// 校验流转任务配置合法
	private void validJobForm(JobForm.JobCreateForm form) {
		// 1、判断数据源是否存在
		DsBean fromDsBean = dsRepository.findByDsId(form.getFromDsId()).orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "来源数据源不存在"));
		dsRepository.findByDsId(form.getToDsId()).orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "目标数据源不存在"));
		// 2、判断流转任务名称是否重复
		jobRepository.findByName(form.getJobName()).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "流转任务名称重复"));

		// 3、判断增量模式下是否有增量字段
		if (MetaConstants.JobSyncMode.INCREMENT_MODE.equals(form.getSyncMode().getMode()) && ObjectUtils.isEmpty(form.getSyncMode().getIncreateField())) {
			throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "增量模式必须指定增量字段");
		}
		// 4、判断增量模式下是否是时间类型或数值类型
		if (MetaConstants.JobSyncMode.INCREMENT_MODE.equals(form.getSyncMode().getMode())) {
			try {
				IDsReader dsReader = DsDriverFactory.getDsReader(dsService.getConnectId(fromDsBean));
				Boolean isIncremental = dsReader.judgeIncrementalField(fromDsBean.getDatabase(), fromDsBean.getSchema(), form.getFromTbName(), form.getSyncMode().getIncreateField());
				if (!isIncremental) {
					throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "增量字段必须是日期或数值类型");
				}
			} catch (Exception e) {
				throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, e.getMessage());
			}
		}
		// 5、配置流转任务定时表达式
		if (ObjectUtils.isEmpty(form.getSchedulerConf())) {
			throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "流转任务需要配置crontab表达式");
		}
	}

	private String getXtbId(String tbName, String dsId) {
		DsTbBean xtbBean = dsTbRepository.findTopByNameAndDsId(tbName, dsId);
		if (!ObjectUtils.isEmpty(xtbBean)) {
			return xtbBean.getTbId();
		}
		return dsService.xtbCreate(tbName, dsId);
	}


	@Override
	public DataTransJobDetail getJobExecInfo(String jobId) {
		JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
		List<JobForm.FieldMappingForm> fieldMappingForms = JsonUtils.toList(jobBean.getConfig(), JobForm.FieldMappingForm.class);

		DataTransJobDetail.SyncUnit syncUnit = DataTransJobDetail.SyncUnit
				.builder()
				.reader(this.getReader(jobBean, fieldMappingForms))
				.writer(this.getWriter(jobBean, fieldMappingForms))
				.build();
		return DataTransJobDetail.builder().jobId(jobId).cover(jobBean.getCover()).syncUnits(Collections.singletonList(syncUnit)).build();
	}

	@SneakyThrows
	private DataTransJobDetail.Reader getReader(JobBean jobBean,
												List<JobForm.FieldMappingForm> jobConf) {

		DsBean fromDs = dsRepository
				.findByDsId(jobBean.getReaderDsId())
				.orElseThrow(
						() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "from ds not exist")
				);
		// 1、流转任务来源表字段列表
		List<DataTransJobDetail.Column> fromCols = jobConf.stream()
				.filter(x -> StringUtils.isNotEmpty(x.getSourceField()) && StringUtils.isNotEmpty(x.getTargetField()))
				.map(x -> DataTransJobDetail.Column.builder()
						.name(x.getSourceField())
						.build())
				.collect(Collectors.toList());
		// 2、检查数据表是否存在
		DsTbBean fromTbBean = dsTbRepository.findByTbId(jobBean.getFromTbId())
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.XTB_NOT_EXISTS, "xtb not found"));

		// 3、获取数据源对应driver驱动
		IDsReader dsReader = DsDriverFactory.getDsReader(dsService.getConnectId(fromDs));

		// 4、获取对应增量条件
		Map<String, String> typeMappings = dsReader.getFields(fromDs.getDatabase(), fromDs.getSchema(), fromTbBean.getName())
				.stream().collect(Collectors.toMap(TableField::getName, TableField::getRawType));
		JobForm.SyncModeForm syncModeForm = JsonUtils.toObject(jobBean.getSyncMode(), JobForm.SyncModeForm.class);

		DataTransJobDetail.Sync.SyncCondition syncCond = this.getSyncCond(syncModeForm, fromCols, typeMappings);

		DataTransJobDetail.Sync sync = DataTransJobDetail.Sync
				.builder()
				.type(syncModeForm.getMode())
				.syncCondition(syncCond)
				.build();

		return DataTransJobDetail.Reader
				.builder()
				.connectId(dsService.getConnectId(fromDs))
				.type(MetaConstants.DsType.TYPE_TO_DB_NAME_MAP.get(fromDs.getType()))
				.schema(fromDs.getDatabase())
				.sync(sync)
				.maxValue(syncModeForm.getIncreateValue())
				.tableName(fromTbBean.getName())
				.columns(fromCols)
				.build();
	}

	private DataTransJobDetail.Sync.SyncCondition getSyncCond(JobForm.SyncModeForm exportMode,
															  List<DataTransJobDetail.Column> syncFields, Map<String, String> typeMappings) {
		DataTransJobDetail.Sync.SyncCondition syncCon = null;
		if ("increment".equalsIgnoreCase(exportMode.getMode())) {
			for (DataTransJobDetail.Column field : syncFields) {
				if (!ObjectUtils.nullSafeEquals(field.getName(), exportMode.getIncreateField())) {
					continue;
				}

				String synFieldType = typeMappings.getOrDefault(field.getName(), "string");
				syncCon = DataTransJobDetail.Sync.SyncCondition.builder()
						.field(field.getName())
						.fieldType(synFieldType)
						.start(DataTransJobDetail.Sync.SyncCondition.Conditon
								.builder()
								.enable(1)
								.operator(">")
								.value(exportMode.getIncreateValue())
								.build())
						.end(DataTransJobDetail.Sync.SyncCondition.Conditon
								.builder()
								.enable(0)
								.build())
						.build();
			}
		}

		return syncCon;
	}

	@SneakyThrows
	private DataTransJobDetail.Writer getWriter(JobBean jobBean,
												List<JobForm.FieldMappingForm> jobConf) {

		DsBean toDs = dsRepository
				.findByDsId(jobBean.getWriterDsId())
				.orElseThrow(
						() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "to ds not exist")
				);


		List<DataTransJobDetail.Column> toCols = jobConf
				.stream()
				.map(x -> DataTransJobDetail
						.Column
						.builder()
						.name(x.getTargetField())
						.build()
				)
				.collect(Collectors.toList());

		DsTbBean tbBean = dsTbRepository.findByTbId(jobBean.getToTbId())
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.TB_NOT_EXISTS, "流转任务目标表不存在"));

		return DataTransJobDetail.Writer.builder()
				.schema(toDs.getDatabase()).connectId(dsService.getConnectId(toDs))
				.type(MetaConstants.DsType.TYPE_TO_DB_NAME_MAP.get(toDs.getType()))
				.tableName(tbBean.getName()).columns(toCols).build();
	}


	@Transactional(rollbackFor = Exception.class)
	@Override
	public String updateJobStatus(JobStateForm jobStateForm) {
		JobBean jobBean = jobRepository.findByJobId(jobStateForm.getJobId())
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
		int status = jobStateForm.getJobStatus();

		// 1、实时推送流转进度
		ProducerAdapterForm producerAdapterForm = new ProducerAdapterForm();
		producerAdapterForm.setType(MessageHubConstants.REDIS_STREAM_TYPE);
		producerAdapterForm.setTopic(MessageHubConstants.JOB_PROGRESS_TOPIC);
		producerAdapterForm.setGroup(MessageHubConstants.GLOBAL_COMMON_GROUP);
		producerAdapterForm.setMessage(
				JsonUtils.toJson(
						JobDto.StatusRefresh.builder()
								.status(status)
								.jobId(jobBean.getJobId())
								.build()
				)
		);
		messageHubService.produce(producerAdapterForm);

		// 2、存储流转任务状态
		JobDto.DataCountDto countVo = JobDto.DataCountDto.builder()
				.allCount(jobStateForm.getAllCount())
				.appendCount(jobStateForm.getAppendCount())
				.filterCount(jobStateForm.getFilterCount())
				.build();
		jobBean.setStatus(status);
		jobBean.setCount(JsonUtils.toJson(countVo));
		jobBean.setErrorMsg(StringUtils.equalsIgnoreCase(jobStateForm.getErrmsg(), "success") ? "任务成功"
				: jobStateForm.getErrmsg() == null ? "" : jobStateForm.getErrmsg());
		jobRepository.save(jobBean);

		// 3、保存流转任务执行日志
		if (!ObjectUtils.isEmpty(jobStateForm.getErrmsg())) {
			jobLogRepository.save(JobLogBean.builder()
					.jobId(jobStateForm.getJobId())
					.startTime(new Timestamp(jobStateForm.getStartTime()))
					.status(ObjectUtils.nullSafeEquals(status, FAILED) ? 1 : 0)
					.endTime(ObjectUtils.isEmpty(jobStateForm.getEndTime()) ? null : new Timestamp(jobStateForm.getEndTime()))
					.costTime(ObjectUtils.isEmpty(jobStateForm.getEndTime()) ? 0 : (int) ((jobStateForm.getEndTime() - jobStateForm.getStartTime()) / 1000))
					.errorMsg(StringUtils.equalsIgnoreCase(jobStateForm.getErrmsg(), "success") ? "任务成功" : jobStateForm.getErrmsg())
					.count(JsonUtils.toJson(countVo))
					.isDel(0)
					.build());
		}
		return jobBean.getJobId();
	}


	@Override
	public String updateJobTaskRel(String jobId, String taskId) {
		JobBean jobBean = jobRepository.findByJobId(jobId)
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));

		jobBean.setTaskId(taskId);
		jobRepository.save(jobBean);
		return jobBean.getJobId();
	}


	@Override
	public void updateSyncMode(JobForm.SyncModifyForm syncMode) {
		JobBean jobBean = jobRepository.findByJobId(syncMode.getJobId())
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
		JobForm.SyncModeForm syncModeForm = JsonUtils.toObject(jobBean.getSyncMode(), JobForm.SyncModeForm.class);
		syncModeForm.setIncreateValue(syncMode.getIncreateValue());
		jobBean.setSyncMode(JsonUtils.toJson(syncModeForm));
		jobRepository.save(jobBean);
	}

	@Override
	public String cascadeJob(String jobId) {
		jobRelationRepository.findSubJob(jobId).stream()
				.sorted(Comparator.comparingInt(JobRelationBean::getPriority))
				.forEach(jobRelationBean -> this.jobExec(jobRelationBean.getSubJobId()));
		return jobId;
	}


	// 创建任务后再手动调用xxl-job触发函数，解决异步调用mysql事务未提交，xxl-job任务已出发问题
	public String jobCreateProxy(JobForm.JobCreateForm form) {
		String jobId = this.jobCreate(form);
		// 1、创建后开启任务
		jobClientApi.start(jobId);
		// 2、默认触发一次任务
		jobClientApi.trigger(jobId, DataTransJobParam.builder().jobId(jobId).build());
		return jobId;
	}

	public void jobExec(String jobId) {
		JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "任务不存在"));
		if (jobBean.getStatus() == JOB_STATUS_SYNC) {
			throw new DatalinkXServerException(StatusCode.JOB_IS_RUNNING, "任务已在运行中，请勿重复触发");
		}

		if (jobBean.getStatus() == JOB_STATUS_STOP) {
			throw new DatalinkXServerException(StatusCode.SYNC_STATUS_ERROR, "任务处于停止状态");
		}

		jobBean.setStatus(JOB_STATUS_SYNC);

		// 如果xxl-job未创建任务，新建一个
		if (!jobClientApi.isXxljobExist(jobId)) {
			String xxlJobId = jobClientApi.add(jobId, jobBean.getCrontab(), DataTransJobParam.builder().jobId(jobId).build());
			jobBean.setXxlId(xxlJobId);
			jobClientApi.start(jobId);
		}

		jobRepository.save(jobBean);
		jobClientApi.trigger(jobId, DataTransJobParam.builder().jobId(jobId).build());
	}


	public JobVo.JobInfoVo info(String jobId) {
		JobBean jobBean = jobRepository.findByJobId(jobId)
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));

		List<String> tbIds = new ArrayList<String>() {{
			add(jobBean.getFromTbId());
			add(jobBean.getToTbId());
		}};

		Map<String, String> dsTbNameMap = dsTbRepository.findAllByTbIdIn(tbIds)
				.stream()
				.collect(Collectors.toMap(DsTbBean::getTbId, DsTbBean::getName));

		JobVo.JobInfoVo jobInfoVo = JobVo.JobInfoVo
				.builder()
				.jobId(jobBean.getJobId())
				.jobName(jobBean.getName())
				.fromDsId(jobBean.getReaderDsId())
				.toDsId(jobBean.getWriterDsId())
				.fromTbName(dsTbNameMap.get(jobBean.getFromTbId()))
				.toTbName(dsTbNameMap.get(jobBean.getToTbId()))
				.schedulerConf(jobBean.getCrontab())
				.cover(jobBean.getCover())
				.syncMode(JsonUtils.toObject(jobBean.getSyncMode(), JobForm.SyncModeForm.class))
				.build();

		List<JobForm.FieldMappingForm> fieldMappingForms = JsonUtils.toList(jobBean.getConfig(), JobForm.FieldMappingForm.class);
		jobInfoVo.setFieldMappings(fieldMappingForms);
		return jobInfoVo;
	}

	public PageVo<List<JobVo.JobPageVo>> page(JobForm.JobPageForm form) {
		PageRequest pageRequest = PageRequest.of(form.getPageNo() - 1, form.getPageSize());
		Page<JobBean> jobBeans = jobRepository.pageQuery(pageRequest);


		List<String> tbIds = new ArrayList<>();
		List<String> dsId = new ArrayList<>();
		jobBeans.getContent().forEach(job -> {
			tbIds.add(job.getFromTbId());
			tbIds.add(job.getToTbId());
			dsId.add(job.getWriterDsId());
			dsId.add(job.getReaderDsId());
		});

		Map<String, String> dsTbNameMap = dsTbRepository.findAllByTbIdIn(tbIds)
				.stream()
				.collect(Collectors.toMap(DsTbBean::getTbId, DsTbBean::getName));

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
					.fromTbName(dsNameMap.get(jobBean.getReaderDsId()) + "." + dsTbNameMap.get(jobBean.getFromTbId()))
					.toTbName(dsNameMap.get(jobBean.getWriterDsId()) + "."  + dsTbNameMap.get(jobBean.getToTbId()))
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
		this.jobRepository.logicDeleteByJobId(jobId);
		this.jobLogRepository.logicDeleteByJobId(jobId);
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

	public void jobStop(String jobId) {
		jobRepository.updateJobStatus(jobId, JOB_STATUS_STOP);
		jobClientApi.stop(jobId);
	}

	public List<JobVo.JobId2NameVo> list() {
		return jobRepository.findAll().stream().map(v -> JobVo.JobId2NameVo.builder().JobId(v.getJobId()).jobName(v.getName()).build()).collect(Collectors.toList());
	}
}