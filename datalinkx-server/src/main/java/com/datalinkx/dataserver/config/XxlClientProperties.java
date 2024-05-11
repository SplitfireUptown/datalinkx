package com.datalinkx.dataserver.config;


import com.datalinkx.dataclient.config.ClientConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;



@Component
@ConfigurationProperties(prefix = "xxl-job")
@Data
public class XxlClientProperties {
    private ClientConfig.ServicePropertieBean client;
    private String username;
    private String password;
    private Integer jobGroup;
    private String execHandler;
}
