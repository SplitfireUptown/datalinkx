package com.datalinkx.datajob.config;


import com.datalinkx.dataclient.config.ClientConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "client")
@Data
public class ClientProperties {
    private ClientConfig.ServicePropertieBean dataserver;
    private ClientConfig.ServicePropertieBean flinkx;
    private ClientConfig.ServicePropertieBean flink;
    private ClientConfig.ServicePropertieBean seatunnel;
}
