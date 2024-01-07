package com.datalinkx.dataserver.service.impl;



import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_STOP;
import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SYNC;
import static com.datalinkx.common.utils.IdUtils.genKey;
import static com.datalinkx.common.utils.JsonUtils.toJson;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.datalinkx.common.constants.MessageHubConstants;
import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.base.model.TableField;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.domain.DsTbBean;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.domain.JobLogBean;
import com.datalinkx.dataserver.bean.dto.JobDto;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.client.xxljob.JobClientApi;
import com.datalinkx.dataserver.client.xxljob.request.DataTransJobParam;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.controller.form.JobStateForm;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.dataserver.repository.DsRepository;
import com.datalinkx.dataserver.repository.DsTbRepository;
import com.datalinkx.dataserver.repository.JobLogRepository;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.DtsJobService;
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
	private JobRepository jobRepository;

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
		jobBean.setStatus(MetaConstants.JobStatus.JOB_TABLE_CREATE);
		jobBean.setCrontab(form.getSchedulerConf());
		jobBean.setSyncMode(JsonUtils.toJson(form.getSyncMode()));

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
		jobRepository.save(jobBean);
		return form.getJobId();
	}


	private void validJobForm(JobForm.JobCreateForm form) {
		DsBean fromDsBean = dsRepository.findByDsId(form.getFromDsId()).orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "来源数据源不存在"));
		DsBean toBean = dsRepository.findByDsId(form.getToDsId()).orElseThrow(() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "目标数据源不存在"));


		if (MetaConstants.JobSyncMode.INCREMENT_MODE.equals(form.getSyncMode().getMode()) && ObjectUtils.isEmpty(form.getSyncMode().getIncreateField())) {
			throw new DatalinkXServerException(StatusCode.JOB_CONFIG_ERROR, "增量模式必须指定增量字段");
		}

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
	public DataTransJobDetail getJobExecInfo(String jobId, List<String> tableIds, Boolean tbDetail) {
		JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() ->
				new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
		List<JobForm.FieldMappingForm> fieldMappingForms = JsonUtils.toList(jobBean.getConfig(), JobForm.FieldMappingForm.class);

		DataTransJobDetail.SyncUnit syncUnit = DataTransJobDetail.SyncUnit
				.builder()
				.taskId(jobId)
				.reader(this.getReader(jobBean, fieldMappingForms))
				.writer(this.getWriter(jobBean, fieldMappingForms))
				.build();
		return DataTransJobDetail.builder().jobId(jobId).syncUnits(Collections.singletonList(syncUnit)).build();
	}

	@SneakyThrows
	private DataTransJobDetail.Reader getReader(JobBean jobBean,
												List<JobForm.FieldMappingForm> jobConf) {

		DsBean fromDs = dsRepository
				.findByDsId(jobBean.getReaderDsId())
				.orElseThrow(
						() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "from ds not exist")
				);

		List<DataTransJobDetail.Column> fromCols = jobConf.stream()
				.map(x -> DataTransJobDetail.Column.builder()
						.name(x.getSourceField())
						.build())
				.collect(Collectors.toList());

		DsTbBean tbBean = dsTbRepository.findByTbId(jobBean.getFromTbId()).orElseThrow(() ->
				new DatalinkXServerException(StatusCode.TB_NOT_EXISTS, "xtable not exist"));

		// 处理增量条件
		IDsReader dsReader = DsDriverFactory.getDsReader(dsService.getConnectId(fromDs));
		Map<String, String> typeMappings = dsReader.getFields(fromDs.getDatabase(), fromDs.getSchema(), fromDs.getName())
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
				.tableId(tbBean.getTbId())
				.connectId(dsService.getConnectId(fromDs))
				.type(MetaConstants.DsType.TYPE_TO_DB_NAME_MAP.get(fromDs.getType()))
				.schema(fromDs.getDatabase())
				.sync(sync)
				.maxValue(syncModeForm.getIncreateValue())
				.tableName(tbBean.getName())
				.realName(tbBean.getName())
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
				.findByDsId(jobBean.getReaderDsId())
				.orElseThrow(
						() -> new DatalinkXServerException(StatusCode.DS_NOT_EXISTS, "to ds not exist")
				);


		DsTbBean dsTbBean = dsTbRepository.findByTbId(jobBean.getToTbId())
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.XTB_NOT_EXISTS, "xtb not found"));

		IDsReader dsReader = DsDriverFactory.getDsReader(dsService.getConnectId(toDs));
		Map<String, String> typeMappings = dsReader.getFields(toDs.getDatabase(), toDs.getSchema(), dsTbBean.getName())
				.stream().collect(Collectors.toMap(TableField::getName, TableField::getRawType));

		List<DataTransJobDetail.Column> toCols = jobConf
				.stream()
				.map(x -> DataTransJobDetail
						.Column
						.builder()
						.name(x.getTargetField())
						.type(typeMappings.getOrDefault(x.getTargetField(), x.getMappingValue()))
						.build()
				)
				.collect(Collectors.toList());

		DsTbBean tbBean = dsTbRepository.findByTbId(jobBean.getToTbId())
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.TB_NOT_EXISTS, "xtable not exist"));

		return DataTransJobDetail.Writer.builder()
				.schema(toDs.getDatabase()).connectId(dsService.getConnectId(toDs))
				.tableId(jobBean.getToTbId()).type(MetaConstants.DsType.TYPE_TO_DB_NAME_MAP.get(toDs.getType()))
				.tableName(tbBean.getName()).columns(toCols).build();
	}


	@Transactional(rollbackFor = Exception.class)
	@Override
	public String updateJobStatus(JobStateForm jobStateForm) {
		JobBean jobBean = jobRepository.findByJobId(jobStateForm.getJobId())
				.orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
		int status = jobStateForm.getJobStatus();

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


	/**
	 *  任务创建代理
	 *  解决异步调用事务不同步
	 */
	public String jobCreateProxy(JobForm.JobCreateForm form) {
		String jobId = this.jobCreate(form);
		// 创建完默认执行一次
		// 启动 xxljob
		jobClientApi.start(jobId);
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

		// if xxl-job not exist, then create it.
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
				.fromDsId(jobBean.getReaderDsId())
				.toDsId(jobBean.getWriterDsId())
				.fromTbName(dsTbNameMap.get(jobBean.getFromTbId()))
				.toTbName(dsTbNameMap.get(jobBean.getToTbId()))
				.schedulerConf(jobBean.getCrontab())
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
}