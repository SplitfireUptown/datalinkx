package com.datalinkx.dataserver.config;


import com.datalinkx.dataclient.client.ClientConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


/**
 * @author uptown
 * @Description TODO
 * @createTime 2021年05月27日 10:59:01
 */
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
