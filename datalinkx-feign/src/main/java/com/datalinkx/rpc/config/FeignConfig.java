package com.datalinkx.rpc.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

/**
 * @author: uptown
 * @date: 2025/4/11 22:53
 */
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new XxlClientLoginInterceptor();
    }
}
