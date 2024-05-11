package com.datalinkx.dataserver.config;

import java.util.Timer;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.Resource;

import com.datalinkx.dataserver.service.StreamJobService;
import com.datalinkx.stream.StreamTaskChecker;
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
            String jobId = streamTaskQueue.poll();
            streamJobService.streamJobExec(jobId);
        }
    }
}
