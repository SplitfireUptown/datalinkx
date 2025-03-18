package com.datalinkx.copilot.client;

import com.datalinkx.dataclient.config.ClientConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "client")
@Data
public class OllamaProperties {
    private ClientConfig.ServicePropertieBean ollama;
}
