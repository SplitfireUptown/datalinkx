package com.datalinkx.dataserver.config;


import com.datalinkx.dataclient.client.ClientConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "client")
@Data
public class ClientProperties {
    private ClientConfig.ServicePropertieBean datajob;
}
