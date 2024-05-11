package com.datalinkx.dataclient.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Builder
public class ClientConfig {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ServicePropertieBean {

        private String url;
        private Integer connectTimeoutMs;
        private Integer callTimeoutMs;
        private Integer readTimeoutMs;
        private Boolean logging = false;
        //调用返回的status状态不符合要求的 抛出异常
        private Boolean errorThrow = true;
    }
}
