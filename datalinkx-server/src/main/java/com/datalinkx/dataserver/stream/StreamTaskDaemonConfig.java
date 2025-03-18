package com.datalinkx.dataserver.stream;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataclient.client.datalinkxjob.DatalinkXJobClient;
import com.datalinkx.dataclient.client.flink.FlinkClient;
import com.datalinkx.dataclient.client.flink.response.FlinkJobOverview;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.StreamJobService;
import com.datalinkx.stream.lock.DistributedLock;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;


@Configuration
@Slf4j
public class StreamTaskDaemonConfig implements InitializingBean {


    @Autowired
    StreamJobService streamJobService;

    @Autowired
    JobRepository jobRepository;

    @Resource
    DistributedLock distributedLock;

    @Autowired
    FlinkClient flinkClient;

    @Autowired
    DatalinkXJobClient datalinkXJobClient;


    @Override
    public void afterPropertiesSet() {
    }

    @Transactional(rollbackFor = Exception.class)
    @Scheduled(fixedDelay = 10000) // 每10秒检查
    public void processQueueItems() {
        log.info("start to check stream task status");
        List<JobBean> restartJob = jobRepository.findRestartJob(MetaConstants.JobType.JOB_TYPE_STREAM);

        if (ObjectUtils.isEmpty(restartJob)) {
            return;
        }

        try {
            JsonNode jsonNode = flinkClient.jobOverview();
            Set<String> runningJobIds = JsonUtils.toList(JsonUtils.toJson(jsonNode.get("jobs")), FlinkJobOverview.class)
                    .stream()
                    .filter(task -> "RUNNING".equalsIgnoreCase(task.getState()))
                    .map(FlinkJobOverview::getName)
                    .collect(Collectors.toSet());

            for (JobBean streamTaskBean : restartJob) {
                String jobId = streamTaskBean.getJobId();

                // 如果datalinkx任务同步中，检查flink任务是否存在
                if (MetaConstants.JobStatus.JOB_STATUS_SYNCING == streamTaskBean.getStatus()) {
                    // 如果flink任务不存在，则重新提交任务
                    if (!runningJobIds.contains(jobId)) {

                        this.runStreamTask(jobId);
                        continue;
                    } else {
                        // 如果因为datalinkx挂掉后重启，flink任务正常，datalinkx任务状态正常，判断健康检查线程是否挂掉, 如果挂掉，先停止再重新提交
                        String stringWebResult = datalinkXJobClient.streamJobHealth(jobId).getResult();


                        // 排除掉刚提交的任务
                        Timestamp startTime = streamTaskBean.getStartTime();
                        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

                        long differenceInMillis = Math.abs(currentTime.getTime() - startTime.getTime());
                        if (differenceInMillis > 1 * 1000 * 60 && ObjectUtils.isEmpty(stringWebResult)) {
                            this.retryTime(jobId);
                            streamJobService.pause(jobId);
                            continue;
                        }
                    }
                }

                // 如果flink任务在运行，而datalinkx中的任务状态为停止，以datalinkx的状态为准，手动停掉flink任务
                if (runningJobIds.contains(jobId) && MetaConstants.JobStatus.JOB_STATUS_STOP == streamTaskBean.getStatus()) {
                    streamJobService.pause(jobId);
                }

                // 如果任务是失败，重新提交
                if (MetaConstants.JobStatus.JOB_STATUS_ERROR == streamTaskBean.getStatus()) {
                    this.retryTime(jobId);
                    this.runStreamTask(jobId);
                }

                if (MetaConstants.JobStatus.JOB_STATUS_CREATE == streamTaskBean.getStatus()) {
                    this.runStreamTask(jobId);
                }
            }
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
    }

    /**
     *  增加重试次数
     * @param jobId
     */
    public void retryTime(String jobId) {
        // TODO webhook 通知
        jobRepository.addRetryTime(jobId);
    }

    /**
     * 提交流式任务
     * @param jobId
     */
    public void runStreamTask(String jobId) {
        String lockId = UUID.randomUUID().toString();
        boolean isLock = distributedLock.lock(jobId, lockId, DistributedLock.LOCK_TIME);
        try {
            // 拿到了流式任务的锁就提交任务，任务状态在datalinkx-job提交流程中更改
            if (isLock) {
                // 双重检查
                Optional<JobBean> jobBeanOp = jobRepository.findByJobId(jobId);
                if (!jobBeanOp.isPresent()) {
                    return;
                }

                JsonNode jsonNode = flinkClient.jobOverview();
                Set<String> runningJobIds = JsonUtils.toList(JsonUtils.toJson(jsonNode.get("jobs")), FlinkJobOverview.class)
                        .stream()
                        .filter(task -> "RUNNING".equalsIgnoreCase(task.getState()))
                        .map(FlinkJobOverview::getName)
                        .collect(Collectors.toSet());
                if (runningJobIds.contains(jobId)) {
                    return;
                }

                streamJobService.streamJobExec(jobId, jobId);
                this.retryTime(jobId);
            }
        } catch (Exception e){
            // 成功一直持有锁，失败需要释放锁，失败也不需要放入队列，定时任务会从db中扫描出来
            distributedLock.unlock(jobId, jobId);
        }
    }
}
