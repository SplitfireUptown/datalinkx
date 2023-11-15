package com.datalinkx.messagehub.config.topic;

import java.util.Timer;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConditionalOnProperty(prefix = "topic",name = "daemon",havingValue = "true")
@Slf4j
public class TopicDaemonWarden implements InitializingBean {

    @Resource
    TopicReloadTask topicReloadTask;


    @Bean
    public void topicReload() {
        Timer timer = new Timer();
        log.info("topic daemon warden reload start");
        timer.schedule(topicReloadTask, 60000, 60000);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        topicReloadTask.run();
    }
}
