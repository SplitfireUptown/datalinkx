package com.datalinkx.copilot.mcp.service;

import com.datalinkx.common.result.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.ChatResponse;
import org.noear.solon.rx.SimpleSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Service
public class MCPChatService {

    @Autowired
    ChatModel mcpChatModel;

    @Value("${llm.mcp.inner_prompt}")
    String innerPrompt;

    public String chat(String question) {
        String result;
        try {
            result = mcpChatModel.prompt(question).call().getMessage().getContent();
        } catch (Exception e) {
            result = "datalinkx-copilot-mcp 模型调用异常: " + e.getMessage();
            log.error(e.getMessage(), e);
        }
        return result;
    }

    public Flux<WebResult<String>> streamChat(String question) {
        Sinks.Many<WebResult<String>> sink = Sinks.many().unicast().onBackpressureBuffer();
        String prompt = String.format("## 问题：%s\n ## 输出规则：%s", question, innerPrompt);

        // 订阅模型的流式响应
        mcpChatModel.prompt(prompt)
                .stream()
                .subscribe(
                        new SimpleSubscriber<ChatResponse>()
                        .doOnNext(resp -> {
                            sink.tryEmitNext(WebResult.of(resp.getMessage().getContent()));
                        })
                        .doOnComplete(sink::tryEmitComplete)
                        .doOnError(err -> {
                            log.error("datalinkx-copilot-mcp 模型调用异常" + err.getMessage(), err);
                            sink.tryEmitComplete();
                        })
                );
        return sink.asFlux();
    }
}
