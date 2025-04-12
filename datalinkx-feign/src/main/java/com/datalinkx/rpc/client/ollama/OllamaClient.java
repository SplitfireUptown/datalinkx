package com.datalinkx.rpc.client.ollama;

import com.datalinkx.rpc.client.ollama.request.ChatReq;
import com.datalinkx.rpc.client.ollama.request.EmbeddingReq;
import com.datalinkx.rpc.client.ollama.response.ChatResult;
import com.datalinkx.rpc.client.ollama.response.EmbeddingResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ollamaClient", url = "${client.ollama.url}")
@ConditionalOnProperty(prefix = "client.ollama", name = "url")
public interface OllamaClient {

    @PostMapping("/api/embeddings")
    EmbeddingResult embedding(@RequestBody EmbeddingReq embeddingReq);

    @PostMapping("/api/chat")
    ChatResult chat(@RequestBody ChatReq chatReq);
}
