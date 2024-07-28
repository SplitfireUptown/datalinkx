package com.datalinkx.dataserver.config;

import java.util.Arrays;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.IdUtils;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.StreamJobService;
import com.datalinkx.stream.StreamTaskChecker;
import com.datalinkx.stream.lock.DistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;


@Configuration
@Slf4j
public class StreamTaskDaemonConfig implements InitializingBean {

    @Resource
    StreamTaskChecker streamTaskChecker;

    @Autowired
    StreamJobService streamJobService;

    @Autowired
    JobRepository jobRepository;

    @Resource
    DistributedLock distributedLock;

    @Autowired
    LinkedBlockingQueue<String> streamTaskQueue;

    @Autowired
    LinkedBlockingQueue<String> stopTaskQueue;

    @Bean
    public void streamTaskCheck() {
        Timer timer = new Timer();
        log.info("topic daemon warden reload start");
        timer.schedule(streamTaskChecker, 30000, 30000);
    }

    @Override
    public void afterPropertiesSet() {
        streamTaskChecker.run();
    }

    @Scheduled(fixedDelay = 10000) // 每10秒检查
    public void processQueueItems() {
        log.info("stream task daemon check");
        // 先停止
        while (!stopTaskQueue.isEmpty()) {
            String jobId = stopTaskQueue.poll();
            streamJobService.pause(jobId);
        }

        // 再启动
        while (!streamTaskQueue.isEmpty()) {
            String lockId = IdUtils.generateShortUuid();
            String jobId = streamTaskQueue.poll();

            boolean isLock = distributedLock.lock(jobId, lockId, DistributedLock.LOCK_TIME);
            try {
                // 拿到了流式任务的锁就提交任务，任务状态在datalinkx-job提交流程中更改
                if (isLock) {
                    // 双重检查
                    Optional<JobBean> jobBeanOp = jobRepository.findByJobId(jobId);
                    if (!jobBeanOp.isPresent()) {
                        continue;
                    }
                    if (!Arrays.asList(
                            MetaConstants.JobStatus.JOB_STATUS_SYNCING,
                            MetaConstants.JobStatus.JOB_STATUS_ERROR).contains(jobBeanOp.get().getStatus())) {
                        continue;
                    }
                    streamJobService.streamJobExec(jobId, lockId);
                }
            } catch (Exception e){
                // 成功一直持有锁，失败需要释放锁，失败也不需要放入队列，定时任务会从db中扫描出来
                distributedLock.unlock(jobId, lockId);
            }
        }
    }
}
