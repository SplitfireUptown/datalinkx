package com.datalinkx.datajob.action;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataclient.client.flink.FlinkClient;
import com.datalinkx.dataclient.client.flink.response.FlinkJobStatus;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.datajob.job.ExecutorStreamJobHandler;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.model.StreamFlinkActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.stream.lock.DistributedLock;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: uptown
 * @date: 2024/4/27 14:23
 */
@Slf4j
@Component
public class StreamDataTransferAction extends AbstractDataTransferAction<DataTransJobDetail, StreamFlinkActionMeta> {
    public static ThreadLocal<Long> START_TIME = new ThreadLocal<>();
    @Autowired
    FlinkClient flinkClient;

    @Autowired
    DatalinkXServerClient datalinkXServerClient;

    @Autowired
    ExecutorStreamJobHandler streamExecutorJobHandler;

    @Autowired
    DistributedLock distributedLock;

    @Override
    protected void begin(DataTransJobDetail info) {
        // 修改任务状态
        START_TIME.set(new Date().getTime());
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(info.getJobId())
                .jobStatus(MetaConstants.JobStatus.JOB_STATUS_SYNCING).startTime(START_TIME.get())
                .build());
    }

    @Override
    protected void end(StreamFlinkActionMeta unit, int status, String errmsg) {
        log.info(String.format("stream job jobid: %s, end to transfer", unit.getJobId()));
        // 修改任务状态，存储checkpoint
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(unit.getJobId())
                .jobStatus(status).endTime(new Date().getTime()).startTime(START_TIME.get())
                .checkpoint(unit.getCheckpoint())
                .errmsg(errmsg)
                .build());
    }

    @Override
    protected void beforeExec(StreamFlinkActionMeta unit) throws Exception {
    }

    @Override
    protected void execute(StreamFlinkActionMeta unit) throws Exception {
        Map<String, Object> commonSettings = unit.getCommonSettings();
        commonSettings.put("savePointPath", unit.getCheckpoint());
        String taskId = streamExecutorJobHandler.execute(unit.getJobId(), unit.getReaderDsInfo(), unit.getWriterDsInfo(), commonSettings);
        unit.setTaskId(taskId);
        // 更新task
        datalinkXServerClient.updateJobTaskRel(unit.getJobId(), taskId);
    }

    @Override
    protected boolean checkResult(StreamFlinkActionMeta unit) {
        String taskId = unit.getTaskId();
        FlinkJobStatus flinkJobStatus = JsonUtils.toObject(JsonUtils.toJson(flinkClient.jobStatus(taskId)), FlinkJobStatus.class);
        String state = flinkJobStatus.getState();
        if ("failed".equalsIgnoreCase(state)) {
            String errorMsg = "stream data-transfer task failed.";

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

        if ("finished".equalsIgnoreCase(state)) {
            return true;
        }
        // 看门狗，续约分布式锁，防止其他节点重复提交任务
        distributedLock.renewLock(unit.getJobId(), unit.getLockId(), DistributedLock.LOCK_TIME);

        // 流式任务不用检测太频繁，歇会
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }

        return false;
    }

    @Override
    protected void afterExec(StreamFlinkActionMeta unit, boolean success) {
        // 任务未提交成功
        if (ObjectUtils.isEmpty(unit.getTaskId())) {
            return;
        }
        // 记录checkpoint
        JsonNode checkpointResult = flinkClient.jobCheckpoint(unit.getTaskId());
        if (!ObjectUtils.isEmpty(checkpointResult)) {
            JsonNode latestResult = checkpointResult.get("latest");
            if (!ObjectUtils.isEmpty(latestResult)) {
                JsonNode savepoint = latestResult.get("savepoint");
                if (!ObjectUtils.isEmpty(savepoint) && !ObjectUtils.isEmpty(savepoint.get("external_path"))) {
                    String checkpointPath = savepoint.get("external_path").toString();
                    unit.setCheckpoint(checkpointPath);
                }
            }
        }
    }

    @SneakyThrows
    @Override
    protected StreamFlinkActionMeta convertExecUnit(DataTransJobDetail info) {
        Object readerDsInfo = DsDriverFactory.getStreamDriver(info.getSyncUnit().getReader().getConnectId()).getReaderInfo(info.getSyncUnit().getReader());

        // 实时任务的writer不一定是流式数据源
        Object writerDsInfo;
        if (MetaConstants.DsType.STREAM_DB_LIST.contains(info.getSyncUnit().getWriter().getType())) {
            writerDsInfo = DsDriverFactory.getStreamDriver(info.getSyncUnit().getWriter().getConnectId()).getWriterInfo(info.getSyncUnit().getWriter());
        } else {
            FlinkActionMeta writerMeta = FlinkActionMeta.builder()
                    .writer(
                            DataTransJobDetail
                                    .Writer
                                    .builder()
                                    .type(info.getSyncUnit().getWriter().getType())
                                    .connectId(info.getSyncUnit().getWriter().getConnectId())
                                    .tableName(info.getSyncUnit().getWriter().getTableName())
                                    .schema(info.getSyncUnit().getWriter().getSchema())
                                    .columns(info.getSyncUnit().getWriter().getColumns())
                                    .batchSize(info.getSyncUnit().getWriter().getBatchSize())
                                    .build()
                    )
                    .build();
            writerDsInfo = DsDriverFactory.getDsWriter(info.getSyncUnit().getWriter().getConnectId()).getWriterInfo(writerMeta);
        }

        return StreamFlinkActionMeta.builder()
                .writerDsInfo(JsonUtils.toJson(writerDsInfo))
                .readerDsInfo(JsonUtils.toJson(readerDsInfo))
                .checkpoint(info.getSyncUnit().getCheckpoint())
                .commonSettings(info.getSyncUnit().getCommonSettings())
                .jobId(info.getJobId())
                .lockId(info.getLockId())
                .build();
    }
}
