package com.datalinkx.dataserver.config;

import com.datalinkx.dataserver.config.aop.LogicDeleteAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JPAConfig {
    @Bean
    public LogicDeleteAspect deleteAspect() {
        return new LogicDeleteAspect();
    }
}
