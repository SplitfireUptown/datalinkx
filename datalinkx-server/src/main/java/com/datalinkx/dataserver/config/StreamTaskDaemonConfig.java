package com.datalinkx.dataserver.config;

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import com.datalinkx.common.utils.IdUtils;
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
    LinkedBlockingQueue<String> streamTaskQueue;

    @Autowired
    StreamJobService streamJobService;

    @Resource
    DistributedLock distributedLock;


    @Bean
    public void streamTaskCheck() {
        Timer timer = new Timer();
        log.info("topic daemon warden reload start");
        timer.schedule(streamTaskChecker, 30000, 30000);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        streamTaskChecker.run();
    }

    @Scheduled(fixedDelay = 10000) // 每10秒检查
    public void processQueueItems() {
        while (!streamTaskQueue.isEmpty()) {
            String lockId = IdUtils.generateShortUuid();
            String jobId = streamTaskQueue.poll();

            boolean isLock = distributedLock.lock(jobId, lockId, 10);
            try {
                // 拿到了流式任务的锁就提交任务，任务状态在datalinkx-job提交流程中更改
                if (isLock) {
                    streamJobService.streamJobExec(jobId);
                }
            } finally {
                // 无论提交任务成功还是失败都要释放锁
                distributedLock.unlock(jobId, lockId);
            }
        }
    }
}
