package com.datalinkx.copilot.mcp.service;

import com.datalinkx.common.result.WebResult;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.chat.ChatModel;
import org.noear.solon.ai.chat.ChatResponse;
import org.noear.solon.rx.SimpleSubscriber;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Slf4j
@Service
public class MCPChatService {

    @Autowired
    ChatModel mcpChatModel;

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
        // 创建支持背压的unicast sink（单播，背压缓冲）
        Sinks.Many<WebResult<String>> sink = Sinks.many().unicast().onBackpressureBuffer();

        // 订阅模型的流式响应
        mcpChatModel.prompt(question)
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
