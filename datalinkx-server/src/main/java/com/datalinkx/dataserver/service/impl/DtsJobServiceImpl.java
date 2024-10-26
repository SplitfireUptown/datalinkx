package com.datalinkx.dataserver.service.impl;

import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SYNCING;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
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
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.domain.JobLogBean;
import com.datalinkx.dataserver.bean.domain.JobRelationBean;
import com.datalinkx.dataserver.bean.dto.JobDto;
import com.datalinkx.dataserver.client.xxljob.JobClientApi;
import com.datalinkx.dataserver.client.xxljob.request.XxlJobParam;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.controller.form.JobStateForm;
import com.datalinkx.dataserver.repository.DsRepository;
import com.datalinkx.dataserver.repository.JobLogRepository;
import com.datalinkx.dataserver.repository.JobRelationRepository;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.DtsJobService;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.base.model.DbTableField;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.messagehub.bean.form.ProducerAdapterForm;
import com.datalinkx.messagehub.service.MessageHubService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

/**
 * @author: uptown
 * @date: 2024/4/25 22:36
 */
@Service
public class DtsJobServiceImpl implements DtsJobService {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobRelationRepository jobRelationRepository;


    @Autowired
    JobLogRepository jobLogRepository;


    @Autowired
    DsServiceImpl dsServiceImpl;

    @Autowired
    DsRepository dsRepository;

    @Resource(name = "messageHubServiceImpl")
    MessageHubService messageHubService;

    @Autowired
    JobClientApi jobClientApi;

    @Value("${data-transfer.fetch-size:1000}")
    Integer fetchSize;

    @Value("${data-transfer.stream-batch-size:10}")
    Integer streamBatchSize;

    @Value("${data-transfer.query-time-out:10000}")
    Integer queryTimeOut;

    @Override
    public DataTransJobDetail getJobExecInfo(String jobId) {
        JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
        List<JobForm.FieldMappingForm> fieldMappingForms = JsonUtils.toList(jobBean.getConfig(), JobForm.FieldMappingForm.class);

        DataTransJobDetail.SyncUnit syncUnit = DataTransJobDetail.SyncUnit
                .builder()
                .reader(this.getReader(jobBean, fieldMappingForms))
                .writer(this.getWriter(jobBean, fieldMappingForms))
                .build();
        return DataTransJobDetail.builder().jobId(jobId).cover(jobBean.getCover()).syncUnit(syncUnit).build();
    }

    @Override
    public DataTransJobDetail getStreamJobExecInfo(String jobId) {
        JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
        List<DataTransJobDetail.Column> columns = JsonUtils.toList(jobBean.getConfig(), JobForm.FieldMappingForm.class).stream()
                .filter(x -> StringUtils.isNotEmpty(x.getSourceField()) && StringUtils.isNotEmpty(x.getTargetField()))
                .map(x -> DataTransJobDetail.Column.builder()
                        .name(x.getSourceField())
                        .build())
                .collect(Collectors.toList());

        List<String> dsIds = new ArrayList<>(Arrays.asList(jobBean.getReaderDsId(), jobBean.getWriterDsId()));
        Map<String, DsBean> dsId2Object = dsRepository.findAllByDsIdIn(dsIds)
                .stream()
                .collect(Collectors.toMap(DsBean::getDsId, v -> v));

        DataTransJobDetail.Reader reader = DataTransJobDetail.Reader.builder()
                .tableName(jobBean.getFromTbId())
                .columns(columns)
                .connectId(dsServiceImpl.getConnectId(dsId2Object.get(jobBean.getReaderDsId())))
                .type(MetaConstants.DsType.TYPE_TO_DB_NAME_MAP.get(dsId2Object.get(jobBean.getReaderDsId()).getType()))
                .build();

        DataTransJobDetail.Writer writer = DataTransJobDetail.Writer.builder()
                .tableName(jobBean.getToTbId())
                .columns(columns)
                .connectId(dsServiceImpl.getConnectId(dsId2Object.get(jobBean.getWriterDsId())))
                .batchSize(streamBatchSize)
                .type(MetaConstants.DsType.TYPE_TO_DB_NAME_MAP.get(dsId2Object.get(jobBean.getWriterDsId()).getType()))
                .build();

        DataTransJobDetail.SyncUnit syncUnit = DataTransJobDetail.SyncUnit
                .builder()
                .reader(reader)
                .writer(writer)
                .checkpoint(jobBean.getCheckpoint())
                .build();
        return DataTransJobDetail.builder().jobId(jobId).syncUnit(syncUnit).build();
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

        // 3、获取数据源对应driver驱动
        IDsReader dsReader = DsDriverFactory.getDsReader(dsServiceImpl.getConnectId(fromDs));

        // 4、获取对应增量条件
        Map<String, String> typeMappings = dsReader.getFields(fromDs.getDatabase(), fromDs.getSchema(), jobBean.getFromTbId())
                .stream().collect(Collectors.toMap(DbTableField::getName, DbTableField::getType));
        JobForm.SyncModeForm syncModeForm = JsonUtils.toObject(jobBean.getSyncMode(), JobForm.SyncModeForm.class);

        DataTransJobDetail.Sync.SyncCondition syncCond = this.getSyncCond(syncModeForm, fromCols, typeMappings);

        DataTransJobDetail.Sync sync = DataTransJobDetail.Sync
                .builder()
                .type(syncModeForm.getMode())
                .syncCondition(syncCond)
                .queryTimeOut(queryTimeOut)
                .fetchSize(fetchSize)
                .build();

        return DataTransJobDetail.Reader
                .builder()
                .connectId(dsServiceImpl.getConnectId(fromDs))
                .type(MetaConstants.DsType.TYPE_TO_DB_NAME_MAP.get(fromDs.getType()))
                .schema(fromDs.getDatabase())
                .sync(sync)
                .maxValue(syncModeForm.getIncreateValue())
                .tableName(jobBean.getFromTbId())
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

        return DataTransJobDetail.Writer.builder()
                .schema(toDs.getDatabase()).connectId(dsServiceImpl.getConnectId(toDs))
                .type(MetaConstants.DsType.TYPE_TO_DB_NAME_MAP.get(toDs.getType()))
                .tableName(jobBean.getToTbId()).columns(toCols).build();
    }



    @Transactional(rollbackFor = Exception.class)
    @Override
    public String updateJobStatus(JobStateForm jobStateForm) {
        JobBean jobBean = jobRepository.findByJobId(jobStateForm.getJobId())
                .orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
        int status = jobStateForm.getJobStatus();

        // 1、实时推送流转进度
//        if (!MetaConstants.JobType.JOB_TYPE_STREAM.equals(jobBean.getType())) {
//            ProducerAdapterForm producerAdapterForm = new ProducerAdapterForm();
//            producerAdapterForm.setType(MessageHubConstants.REDIS_STREAM_TYPE);
//            producerAdapterForm.setTopic(MessageHubConstants.JOB_PROGRESS_TOPIC);
//            producerAdapterForm.setGroup(MessageHubConstants.GLOBAL_COMMON_GROUP);
//            producerAdapterForm.setMessage(
//                    JsonUtils.toJson(
//                            JobDto.StatusRefresh.builder()
//                                    .status(status)
//                                    .jobId(jobBean.getJobId())
//                                    .build()
//                    )
//            );
//            messageHubService.produce(producerAdapterForm);
//        }

        // 2、存储流转任务状态
        JobDto.DataCountDto countVo = JobDto.DataCountDto.builder()
                .allCount(jobStateForm.getAllCount())
                .appendCount(jobStateForm.getAppendCount())
                .filterCount(jobStateForm.getFilterCount())
                .build();
        jobBean.setStartTime(ObjectUtils.isEmpty(jobStateForm.getStartTime()) ? null : new Timestamp(jobStateForm.getStartTime()));
        jobBean.setStatus(status);
        jobBean.setCount(JsonUtils.toJson(countVo));
        jobBean.setErrorMsg(StringUtils.equalsIgnoreCase(jobStateForm.getErrmsg(), "success") ? "任务成功"
                : jobStateForm.getErrmsg() == null ? "" : jobStateForm.getErrmsg());
        jobRepository.save(jobBean);

        // 3、保存流转任务执行日志
        if (!ObjectUtils.isEmpty(jobStateForm.getErrmsg())) {
            jobLogRepository.save(JobLogBean.builder()
                    .jobId(jobStateForm.getJobId())
                    .startTime(ObjectUtils.isEmpty(jobStateForm.getStartTime()) ? null : new Timestamp(jobStateForm.getStartTime()))
                    .status(ObjectUtils.nullSafeEquals(status, MetaConstants.JobStatus.JOB_STATUS_ERROR) ? 1 : 0)
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

    public void jobExec(String jobId) {
        JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "任务不存在"));
        if (jobBean.getStatus() == JOB_STATUS_SYNCING) {
            throw new DatalinkXServerException(StatusCode.JOB_IS_RUNNING, "任务已在运行中，请勿重复触发");
        }

//		if (jobBean.getStatus() == JOB_STATUS_STOP) {
//			throw new DatalinkXServerException(StatusCode.SYNC_STATUS_ERROR, "任务处于停止状态");
//		}

        jobBean.setStatus(JOB_STATUS_SYNCING);

        // 如果xxl-job未创建任务，新建一个
        if (!jobClientApi.isXxljobExist(jobId)) {
            String xxlJobId = jobClientApi.add(jobBean.getCrontab(), XxlJobParam.builder().jobId(jobId).build());
            jobBean.setXxlId(xxlJobId);
            jobClientApi.start(jobId);
        }

        jobRepository.save(jobBean);
        jobClientApi.trigger(jobId, XxlJobParam.builder().jobId(jobId).build());
    }
}
