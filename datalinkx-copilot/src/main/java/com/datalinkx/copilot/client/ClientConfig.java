package com.datalinkx.copilot.client;

import com.datalinkx.dataclient.config.DatalinkXClientUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfig {

    @Bean
    public OllamaClient ollamaClient(OllamaProperties ollamaConfig) {
        return DatalinkXClientUtils.createClient("ollama", ollamaConfig.getOllama(), OllamaClient.class);
    }
}
