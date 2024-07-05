package com.datalinkx.stream;

import java.util.List;
import java.util.Set;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataclient.client.datalinkxjob.DatalinkXJobClient;
import com.datalinkx.dataclient.client.flink.FlinkClient;
import com.datalinkx.dataclient.client.flink.response.FlinkJobOverview;
import com.datalinkx.stream.lock.DistributedLock;
import com.datalinkx.stream.task.StreamTaskBean;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
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

    @Bean
    public LinkedBlockingQueue<String> streamTaskQueue() {
        return new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        String querySQL = "SELECT job_id, status FROM JOB where type in (1, 3) and is_del = 0";
        List<StreamTaskBean> streamTaskBeans = jdbcTemplate.queryForList(querySQL).stream().map(item ->
                StreamTaskBean
                .builder()
                .jobId((String) item.get("job_id"))
                .status((Integer) item.get("status"))
                .taskId((String) item.get("task_id"))
                .build()).collect(Collectors.toList());

        for (StreamTaskBean streamTaskBean : streamTaskBeans) {
            // 如果任务是同步中，检查flink任务是否存在
            if (MetaConstants.JobStatus.JOB_STATUS_SYNCING == streamTaskBean.getStatus()) {
                String jobId = streamTaskBean.getJobId();
                try {
                    JsonNode jsonNode = flinkClient.jobOverview();
                    Set<String> runningJobIds = JsonUtils.toList(JsonUtils.toJson(jsonNode.get("jobs")), FlinkJobOverview.class)
                            .stream()
                            .filter(task -> "RUNNING".equalsIgnoreCase(task.getState()))
                            .map(FlinkJobOverview::getName)
                            .collect(Collectors.toSet());
                    if (!runningJobIds.contains(jobId) && !streamTaskQueue.contains(streamTaskBean.getJobId())) {
                        streamTaskQueue.add(streamTaskBean.getJobId());
                    }
                } catch (Throwable t) {
                    log.error(t.getMessage(), t);
                }
            }

            // 如果任务是失败，重新提交
            if (MetaConstants.JobStatus.JOB_STATUS_ERROR == streamTaskBean.getStatus() && !streamTaskQueue.contains(streamTaskBean.getJobId())) {
                streamTaskQueue.add(streamTaskBean.getJobId());
            }
        }
    }
}
