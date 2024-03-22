// CHECKSTYLE:OFF
package com.datalinkx.datajob.action;

import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SUCCESS;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.datalinkx.common.constants.MessageHubConstants;
import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.datajob.bean.JobExecCountDto;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.bean.JobSyncModeForm;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.datajob.client.flink.FlinkClient;
import com.datalinkx.datajob.client.flink.response.FlinkJobAccumulators;
import com.datalinkx.datajob.client.flink.response.FlinkJobStatus;
import com.datalinkx.datajob.job.ExecutorJobHandler;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionParam;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.driver.utils.JobUtils;
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
public class DataTransferAction extends AbstractDataTransferAction<DataTransJobDetail, FlinkActionParam> {
    public static ThreadLocal<Long> startTime = new ThreadLocal<>();
    public static ThreadLocal<Map<String, JobExecCountDto>> countRes = new ThreadLocal<>();
    public static ThreadLocal<Thread> checkThread = new ThreadLocal<>();
    public static final long SLEEP_TIME = 1000L;


    @Autowired
    private ExecutorJobHandler executorJobHandler;

    @Autowired
    private FlinkClient flinkClient;

    @Autowired
    private DatalinkXServerClient datalinkXServerClient;


    @Resource(name = "messageHubServiceImpl")
    MessageHubService messageHubService;

//    public boolean isSupportedDb(String fromDbType, String toDbTYpe) {
//        return dsProperties.getDatasource().contains(fromDbType) && dsProperties.getDatasource().contains(toDbTYpe);
//    }

    @Override
    protected void begin(DataTransJobDetail info) {
        // 更新同步任务的状态
        startTime.set(new Date().getTime());
        countRes.set(new HashMap<>());
        JobExecCountDto jobExecCountDto = new JobExecCountDto();
        log.info(String.format("jobid: %s, start to transfer", info.getJobId()));
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(info.getJobId())
                .jobStatus(MetaConstants.JobStatus.JOB_STATUS_CREATE).startTime(startTime.get()).endTime(null)
                .allCount(jobExecCountDto.getAllCount())
                .appendCount(jobExecCountDto.getAppendCount())
                .filterCount(jobExecCountDto.getFilterCount())
                .build());
    }

    @Override
    protected void end(DataTransJobDetail info, int status, String errmsg) {
        JobExecCountDto jobExecCountDto = new JobExecCountDto();
        log.info(String.format("jobid: %s, end to sync", info.getJobId()));

        Thread thread = DataTransferAction.checkThread.get();
        if (null != thread && thread.isAlive()) {
            thread.interrupt();
        }

        if (countRes.get() != null) {
            countRes.get().forEach((key, value) -> {
                jobExecCountDto.setAllCount(jobExecCountDto.getAllCount() + value.getAllCount());
                jobExecCountDto.setAppendCount(jobExecCountDto.getAppendCount() + value.getAppendCount());
                jobExecCountDto.setFilterCount(jobExecCountDto.getFilterCount() + value.getFilterCount());
            });
        }
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(info.getJobId())
                .jobStatus(status).startTime(startTime.get()).endTime(new Date().getTime())
                .tbTotal(JobUtils.cntx().getTotal()).tbSuccess(JobUtils.cntx().getSuccess())
                .allCount(jobExecCountDto.getAllCount())
                .appendCount(jobExecCountDto.getAppendCount())
                .filterCount(jobExecCountDto.getFilterCount())
                .errmsg(errmsg)
                .build());
        // 父任务执行成功后级联触发子任务
        if (JOB_STATUS_SUCCESS == status) {
            datalinkXServerClient.cascadeJob(info.getJobId());
        }
    }

    @Override
    protected void beforeExec(FlinkActionParam unit) throws Exception {
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
    }


    @Override
    protected void execute(FlinkActionParam unit) throws Exception {
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
            taskId = executorJobHandler.execute(unit.getJobId(), readerStr, writerStr);
            unit.setTaskId(taskId) ;
            // 更新task
            datalinkXServerClient.updateJobTaskRel(unit.getJobId(), taskId);
        } catch (DatalinkXJobException e) {
            log.error("data transfer failed", e);
            throw e;
        }
    }

    @Override
    protected boolean checkResult(FlinkActionParam unitParam) throws DatalinkXJobException {
        String taskId = unitParam.getTaskId();
        if (StringUtils.isEmpty(taskId)) {
            throw new DatalinkXJobException("flink task id is empty.");
        }


        FlinkJobStatus flinkJobStatus = JsonUtils.toObject(JsonUtils.toJson(flinkClient.jobStatus(taskId)), FlinkJobStatus.class);
        String state = flinkJobStatus.getState();

        if ("finished".equalsIgnoreCase(state)) {
            computeRecords(unitParam, flinkJobStatus);
            return true;
        }

        if ("failed".equalsIgnoreCase(state)) {
            String errorMsg = "flink task failed.";

            if (flinkClient != null) {
                JsonNode jsonNode = flinkClient.jobExceptions(taskId);
                if (jsonNode.has("all-exceptions")) {
                    Iterator<JsonNode> exceptions = jsonNode.get("all-exceptions").elements();
                    if (exceptions.hasNext()) {
                        errorMsg = exceptions.next().get("exception").asText();
                    }
                }
            }

            log.error(errorMsg);
            throw new DatalinkXJobException(errorMsg);
        }

        if ("canceled".equalsIgnoreCase(state)) {
            log.error("flink task canceled.");
            throw new DatalinkXJobException("flink task canceled.");
        }

        computeRecords(unitParam, flinkJobStatus);
        return false;
    }

    private void computeRecords(FlinkActionParam unitParam, FlinkJobStatus flinkJobStatus) {
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
        if (countRes.get() == null) {
            countRes.set(new HashMap<>());
        }

        if (countRes.get().get(tableName) == null) {
            countRes.get().put(tableName, new JobExecCountDto());
        }
        return countRes.get().get(tableName);
    }

    @Override
    protected void afterExec(FlinkActionParam unit, boolean success, String errorMsg) {
        // 记录增量记录
        datalinkXServerClient.updateSyncMode(
                JobSyncModeForm.builder()
                        .jobId(unit.getJobId())
                        .increateValue(
                                unit.getReader().getMaxValue()
                        ).build());
        // 同步表状态
        if (success) {
            log.info(String.format("jobid: %s, after from %s to %s",
                    unit.getJobId(), unit.getReader().getTableName(), unit.getWriter().getTableName()));
            try {
                unit.getDsReader().afterRead(unit);
                unit.getDsWriter().afterWrite(unit);
            } catch (Exception e) {
                success = false;
                errorMsg = "数据同步完成，合表失败.";
                log.error(errorMsg, e);
            }

            String tableName = unit.getReader().getTableName();
            getExecCount(tableName).setAllCount(getExecCount(tableName).getAllCount() == null ? 0 : getExecCount(tableName).getAllCount());
            getExecCount(tableName).setAppendCount(getExecCount(tableName).getAppendCount() + unit.getReadRecords());
            getExecCount(tableName).setFilterCount(getExecCount(tableName).getFilterCount() + unit.getWriteRecords());
        }

        if (success) {
            JobUtils.cntx().addSuccess();
        }
    }

    @Override
    protected void cancel(FlinkActionParam unit) {
        if (!ObjectUtils.isEmpty(unit.getTaskId())) {
            try {
                flinkClient.jobStop(unit.getTaskId());
                log.error(String.format("flink task stop: %s", unit.getTaskId()));
            } catch (Exception e) {
                log.error("flink task stop failed: ", e);
            }
        }

    }

    @Override
    protected List<FlinkActionParam> getExecUnitList(DataTransJobDetail jobDetail) {
        return jobDetail.getSyncUnits().stream().map(syncUnit -> FlinkActionParam.builder()
                    .reader(syncUnit.getReader())
                    .writer(syncUnit.getWriter())
                    .jobId(jobDetail.getJobId())
                    .full(jobDetail.getFull())
                    .build()).collect(Collectors.toList());
    }
}
