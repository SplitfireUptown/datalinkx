
package com.datalinkx.datajob.action;

import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SUCCESS;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.Resource;

import com.datalinkx.common.constants.MessageHubConstants;
import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataclient.client.flink.FlinkClient;
import com.datalinkx.dataclient.client.flink.response.FlinkJobAccumulators;
import com.datalinkx.dataclient.client.flink.response.FlinkJobStatus;
import com.datalinkx.datajob.bean.JobExecCountDto;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.bean.JobSyncModeForm;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.datajob.job.ExecutorJobHandler;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.messagehub.bean.form.ProducerAdapterForm;
import com.datalinkx.messagehub.service.MessageHubService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class DataTransferAction extends AbstractDataTransferAction<DataTransJobDetail, FlinkActionMeta> {
    public static ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    public static ThreadLocal<Map<String, JobExecCountDto>> COUNT_RES = new ThreadLocal<>();


    @Autowired
    private ExecutorJobHandler executorJobHandler;

    @Autowired
    private FlinkClient flinkClient;

    @Autowired
    private DatalinkXServerClient datalinkXServerClient;


    @Resource(name = "messageHubServiceImpl")
    MessageHubService messageHubService;


    @Override
    protected void begin(DataTransJobDetail info) {
        // 更新同步任务的状态
        START_TIME.set(new Date().getTime());
        COUNT_RES.set(new HashMap<>());
        JobExecCountDto jobExecCountDto = new JobExecCountDto();
        log.info(String.format("jobid: %s, start to transfer", info.getJobId()));
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(info.getJobId())
                .jobStatus(MetaConstants.JobStatus.JOB_STATUS_CREATE).startTime(START_TIME.get()).endTime(null)
                .allCount(jobExecCountDto.getAllCount())
                .appendCount(jobExecCountDto.getAppendCount())
                .filterCount(jobExecCountDto.getFilterCount())
                .build());
    }

    @Override
    protected void end(FlinkActionMeta unit, int status, String errmsg) {
        JobExecCountDto jobExecCountDto = new JobExecCountDto();
        log.info(String.format("jobid: %s, end to transfer", unit.getJobId()));


        if (COUNT_RES.get() != null) {
            COUNT_RES.get().forEach((key, value) -> {
                jobExecCountDto.setAllCount(jobExecCountDto.getAllCount() + value.getAllCount());
                jobExecCountDto.setAppendCount(jobExecCountDto.getAppendCount() + value.getAppendCount());
                jobExecCountDto.setFilterCount(jobExecCountDto.getFilterCount() + value.getFilterCount());
            });
        }
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(unit.getJobId())
                .jobStatus(status).startTime(START_TIME.get()).endTime(new Date().getTime())
                .allCount(jobExecCountDto.getAllCount())
                .appendCount(jobExecCountDto.getAppendCount())
                .filterCount(jobExecCountDto.getFilterCount())
                .errmsg(errmsg)
                .build());
        // 父任务执行成功后级联触发子任务
        if (JOB_STATUS_SUCCESS == status) {
            datalinkXServerClient.cascadeJob(unit.getJobId());
        }
    }

    @Override
    protected void beforeExec(FlinkActionMeta unit) throws Exception {
        log.info(String.format("jobid: %s, begin from %s to %s", unit.getJobId(), unit.getReader().getTableName(), unit.getWriter().getTableName()));

        // 同步表状态
        IDsReader readDsDriver;
        IDsWriter writeDsDriver;
        try {
            readDsDriver = DsDriverFactory.getDsReader(unit.getReader().getConnectId());
            writeDsDriver = DsDriverFactory.getDsWriter(unit.getWriter().getConnectId());
            unit.setDsReader(readDsDriver);
            unit.setDsWriter(writeDsDriver);
        } catch (Exception e) {
            throw new Exception("driver init error: ", e);
        }
        // 是否覆盖数据
        if (unit.getCover() == 1) {
            writeDsDriver.truncateData(unit);
        }
    }


    @Override
    protected void execute(FlinkActionMeta unit) throws Exception {
        log.info(String.format("jobid: %s, exec from %s#%s to %s#%s",
                unit.getJobId(),
                unit.getReader().getSchema(),
                unit.getReader().getTableName(),
                unit.getWriter().getSchema(),
                unit.getWriter().getTableName())
        );
        String taskId = unit.getTaskId();
        try {
            if (!ObjectUtils.isEmpty(taskId)) {
                return;
            }

            Object reader = unit.getDsReader().getReaderInfo(unit);
            Object writer = unit.getDsWriter().getWriterInfo(unit);

            String readerStr = JsonUtils.toJson(reader);
            String writerStr = JsonUtils.toJson(writer);
            taskId = executorJobHandler.execute(unit.getJobId(), readerStr, writerStr, new HashMap<>());
            unit.setTaskId(taskId) ;
            // 更新task
            datalinkXServerClient.updateJobTaskRel(unit.getJobId(), taskId);
        } catch (DatalinkXJobException e) {
            log.error("data transfer failed", e);
            throw e;
        }
    }

    @Override
    protected boolean checkResult(FlinkActionMeta unitParam) throws DatalinkXJobException {
        String taskId = unitParam.getTaskId();
        if (ObjectUtils.isEmpty(taskId)) {
            throw new DatalinkXJobException("task id is empty.");
        }


        FlinkJobStatus flinkJobStatus = JsonUtils.toObject(JsonUtils.toJson(flinkClient.jobStatus(taskId)), FlinkJobStatus.class);
        String state = flinkJobStatus.getState();

        if ("finished".equalsIgnoreCase(state)) {
            computeRecords(unitParam, flinkJobStatus);
            return true;
        }

        if ("failed".equalsIgnoreCase(state)) {
            String errorMsg = "data-transfer task failed.";

            JsonNode jsonNode = flinkClient.jobExceptions(taskId);
            if (jsonNode.has("all-exceptions")) {
                Iterator<JsonNode> exceptions = jsonNode.get("all-exceptions").elements();
                if (exceptions.hasNext()) {
                    errorMsg = exceptions.next().get("exception").asText();
                }
            }
            log.error(errorMsg);
            throw new DatalinkXJobException(errorMsg);
        }

        if ("canceled".equalsIgnoreCase(state)) {
            log.error("data-transfer task canceled.");
            throw new DatalinkXJobException("data-transfer task canceled.");
        }

        computeRecords(unitParam, flinkJobStatus);
        return false;
    }

    private void computeRecords(FlinkActionMeta unitParam, FlinkJobStatus flinkJobStatus) {
        AtomicInteger readRecords = new AtomicInteger(0);
        AtomicInteger writeRecords = new AtomicInteger(0);
        AtomicInteger errorRecords = new AtomicInteger(0);
        AtomicLong bytes = new AtomicLong(0);

        FlinkJobAccumulators flinkJobAccumulators = JsonUtils.toObject(
                JsonUtils.toJson(
                        flinkClient.jobAccumulators(unitParam.getTaskId())
                ),
                FlinkJobAccumulators.class
        );
        for (FlinkJobAccumulators.UserTaskAccumulator userTaskAccumulator : Optional.ofNullable(flinkJobAccumulators.getUserTaskAccumulators()).orElse(new ArrayList<>())) {
            if ("numWrite".equals(userTaskAccumulator.getName())) {
                writeRecords = new AtomicInteger(Integer.parseInt(userTaskAccumulator.getValue()));
            }
            else if ("numRead".equals(userTaskAccumulator.getName())) {
                readRecords = new AtomicInteger(Integer.parseInt(userTaskAccumulator.getValue()));
            }
            else if ("nErrors".equals(userTaskAccumulator.getName())) {
                errorRecords = new AtomicInteger(Integer.parseInt(userTaskAccumulator.getValue()));
            }
        }

        unitParam.setWriteBytes(bytes.get());
        unitParam.setReadRecords(readRecords.get());
        unitParam.setErrorRecords(errorRecords.get());
        unitParam.setWriteRecords(writeRecords.get() - errorRecords.get());

        // 实时推送流转进度
        ProducerAdapterForm producerAdapterForm = new ProducerAdapterForm();
        producerAdapterForm.setType(MessageHubConstants.REDIS_STREAM_TYPE);
        producerAdapterForm.setTopic(MessageHubConstants.JOB_PROGRESS_TOPIC);
        producerAdapterForm.setGroup(MessageHubConstants.GLOBAL_COMMON_GROUP);
        Map<String, Object> jobProgress = new HashMap<String, Object>() {{
            put("job_id", unitParam.getJobId());
            put("status", 1);
            put("read_records", unitParam.getReadRecords());
            put("write_records", unitParam.getWriteRecords());
        }};
        producerAdapterForm.setMessage(JsonUtils.toJson(jobProgress));
        messageHubService.produce(producerAdapterForm);
    }

    private JobExecCountDto getExecCount(String tableName) {
        if (COUNT_RES.get() == null) {
            COUNT_RES.set(new HashMap<>());
        }

        if (COUNT_RES.get().get(tableName) == null) {
            COUNT_RES.get().put(tableName, new JobExecCountDto());
        }
        return COUNT_RES.get().get(tableName);
    }

    @Override
    protected void afterExec(FlinkActionMeta unit, boolean success) {
        // 记录增量记录
        datalinkXServerClient.updateSyncMode(
                JobSyncModeForm.builder()
                        .jobId(unit.getJobId())
                        .increateValue(
                                unit.getReader().getMaxValue()
                        ).build());
        // 同步表状态
        if (success) {
            log.info(String.format("jobid: %s, after from %s to %s", unit.getJobId(), unit.getReader().getTableName(), unit.getWriter().getTableName()));
            String tableName = unit.getReader().getTableName();
            getExecCount(tableName).setAllCount(getExecCount(tableName).getAllCount() == null ? 0 : getExecCount(tableName).getAllCount());
            getExecCount(tableName).setAppendCount(getExecCount(tableName).getAppendCount() + unit.getReadRecords());
            getExecCount(tableName).setFilterCount(getExecCount(tableName).getFilterCount() + unit.getWriteRecords());
        }
    }

    @Override
    protected FlinkActionMeta convertExecUnit(DataTransJobDetail jobDetail) {
        return FlinkActionMeta.builder()
                    .reader(jobDetail.getSyncUnit().getReader())
                    .writer(jobDetail.getSyncUnit().getWriter())
                    .jobId(jobDetail.getJobId())
                    .cover(jobDetail.getCover())
                    .build();
    }
}
