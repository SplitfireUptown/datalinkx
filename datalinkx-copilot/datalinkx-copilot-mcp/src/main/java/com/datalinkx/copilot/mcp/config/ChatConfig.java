package com.datalinkx.copilot.mcp.config;

import com.datalinkx.copilot.mcp.tools.DataTransferJobTool;
import org.noear.solon.ai.chat.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class ChatConfig {

    @Value("${llm.mcp.base-url}")
    private String baseUrl;

    @Value("${llm.mcp.model}")
    private String model;

    @Value("${llm.mcp.provider}")
    private String provider;

    @Value("${llm.mcp.api-key}")
    private String apiKey;

    @Autowired
    private DataTransferJobTool dataTransferJobTool;

    @Bean
    public ChatModel mcpChatModel() {
        return ChatModel.of(baseUrl)
                .model(model)
                .provider(provider)
                .apiKey(apiKey)
                .defaultToolsAdd(dataTransferJobTool)
                .build();
    }
}
