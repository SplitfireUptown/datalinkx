package com.datalinkx.stream;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.common.utils.IdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataclient.client.datalinkxjob.DatalinkXJobClient;
import com.datalinkx.dataclient.client.flink.FlinkClient;
import com.datalinkx.dataclient.client.flink.response.FlinkJobOverview;
import com.datalinkx.stream.lock.DistributedLock;
import com.datalinkx.stream.task.StreamTaskBean;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@ConditionalOnProperty(prefix = "topic",name = "daemon",havingValue = "true")
@Component
public class StreamTaskChecker extends TimerTask {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    FlinkClient flinkClient;

    @Autowired
    DistributedLock distributedLock;

    @Autowired
    DatalinkXJobClient datalinkXJobClient;

    @Autowired
    LinkedBlockingQueue<String> streamTaskQueue;

    @Autowired
    LinkedBlockingQueue<String> stopTaskQueue;

    @Bean
    public LinkedBlockingQueue<String> streamTaskQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Bean
    public LinkedBlockingQueue<String> stopTaskQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        String querySQL = "SELECT job_id, status FROM JOB where type = 1 and status in (1, 3) and is_del = 0";
        List<StreamTaskBean> streamTaskBeans = jdbcTemplate.queryForList(querySQL).stream().map(item ->
                StreamTaskBean
                .builder()
                .jobId((String) item.get("job_id"))
                .status((Integer) item.get("status"))
                .taskId((String) item.get("task_id"))
                .build()).collect(Collectors.toList());
        if (ObjectUtils.isEmpty(streamTaskBeans)) {
            return;
        }

        try {
            JsonNode jsonNode = flinkClient.jobOverview();
            Set<String> runningJobIds = JsonUtils.toList(JsonUtils.toJson(jsonNode.get("jobs")), FlinkJobOverview.class)
                    .stream()
                    .filter(task -> "RUNNING".equalsIgnoreCase(task.getState()))
                    .map(FlinkJobOverview::getName)
                    .collect(Collectors.toSet());

            for (StreamTaskBean streamTaskBean : streamTaskBeans) {
                // 如果任务是同步中，检查flink任务是否存在
                if (MetaConstants.JobStatus.JOB_STATUS_SYNCING == streamTaskBean.getStatus()) {
                    String jobId = streamTaskBean.getJobId();
                    // 如果flink任务不存在，则重新提交任务
                    if (!runningJobIds.contains(jobId)) {
                        this.addStreamTaskQueue(jobId);
                    } else {
                        // 如果因为datalinkx挂掉后重启，flink任务正常，datalinkx任务状态正常，判断健康检查线程是否挂掉, 如果挂掉，先停止再重新提交
                        String stringWebResult = datalinkXJobClient.streamJobHealth(jobId).getResult();
                        if (ObjectUtils.isEmpty(stringWebResult)) {
                            this.addStopTaskQueue(jobId);
                            this.addStreamTaskQueue(jobId);
                        }
                    }

                    // 如果flink任务在运行，而datalinkx中的任务状态为停止，以datalinkx的状态为准，则手动停掉
                    if (runningJobIds.contains(jobId) && MetaConstants.JobStatus.JOB_STATUS_STOP == streamTaskBean.getStatus()) {
                        this.addStopTaskQueue(jobId);
                    }
                }

                // 如果任务是失败，重新提交
                if (MetaConstants.JobStatus.JOB_STATUS_ERROR == streamTaskBean.getStatus() && !streamTaskQueue.contains(streamTaskBean.getJobId())) {
                    streamTaskQueue.add(streamTaskBean.getJobId());
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    private synchronized void addStreamTaskQueue(String jobId) {
        if (!streamTaskQueue.contains(jobId)) {
            streamTaskQueue.add(jobId);
        }
    }

    private synchronized void addStopTaskQueue(String jobId) {
        if (!stopTaskQueue.contains(jobId)) {
            stopTaskQueue.add(jobId);
        }
    }
}
