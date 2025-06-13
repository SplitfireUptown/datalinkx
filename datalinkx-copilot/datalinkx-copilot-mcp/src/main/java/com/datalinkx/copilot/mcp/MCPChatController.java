package com.datalinkx.copilot.mcp;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.copilot.mcp.service.MCPChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/api/mcp")
public class MCPChatController {

    @Autowired
    MCPChatService mcpChatService;

    @RequestMapping("/chat")
    public WebResult chat(String question) {
        return WebResult.of(mcpChatService.chat(question));
    }

    @RequestMapping(value = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<WebResult<String>> streamChat(String question) {
        return mcpChatService.streamChat(question);
    }
}
