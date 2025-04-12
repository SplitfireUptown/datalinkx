package com.datalinkx.dataserver.config.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;



@Component
@ConfigurationProperties(prefix = "xxl-job")
@Data
public class XxlClientProperties {
    private String username;
    private String password;
    private String execHandler;
    private String executorRouteStrategy;
    private String misfireStrategy;
    private String executorBlockStrategy;
    // 执行器端口，与datalinkx-job配置文件中xxl.job.executor.port相同
    private Integer executorPort;
}
