package com.datalinkx.copilot.service;

import java.util.Collections;
import java.util.UUID;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.copilot.client.OllamaClient;
import com.datalinkx.copilot.client.request.ChatReq;
import com.datalinkx.copilot.client.request.EmbeddingReq;
import com.datalinkx.copilot.client.response.ChatResult;
import com.datalinkx.copilot.client.response.EmbeddingResult;
import com.datalinkx.copilot.llm.LLMUtils;
import com.datalinkx.copilot.vector.ElasticSearchVectorStorage;
import com.datalinkx.sse.config.SseTransformUtil;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    OllamaClient ollamaClient;

    @Autowired
    ElasticSearchVectorStorage elasticSearchStorage;
    @Value("${client.ollama.url}")
    String ollamaUrl;
    @Value("${llm.embedding}")
    String embeddingModel;
    @Value("${llm.model}")
    String chatModel;

    @Override
    public String chat(String question) {
        // 向量召回
        String vectorData = this.callBackQuestion(question);
        // 构建Prompt
        String prompt = ObjectUtils.isEmpty(vectorData) ? LLMUtils.buildPrompt(question) : LLMUtils.buildRAGPrompt(question, vectorData);
        ChatReq chatReq = ChatReq.builder()
                .messages(Collections.singletonList(ChatReq.Content.builder().role("user").content(prompt).build()))
                .stream(false)
                .model(chatModel)
                .temperature(0.1)
                .build();
        ChatResult chat = ollamaClient.chat(chatReq);

        // 大模型对话
        return chat.getMessage().getContent();
    }

    @Override
    public SseEmitter streamChat(String question) {
        // 向量召回
        String vectorData = this.callBackQuestion(question);
        // 构建Prompt
        String prompt = ObjectUtils.isEmpty(vectorData) ? LLMUtils.buildPrompt(question) : LLMUtils.buildRAGPrompt(question, vectorData);
        ChatReq chatReq = ChatReq.builder()
                .messages(Collections.singletonList(ChatReq.Content.builder().role("user").content(prompt).build()))
                .stream(true)
                .model(chatModel)
                .temperature(0.1)
                .build();

        byte[] bodyBytes = JsonUtils.toJson(chatReq).getBytes();
        String largeModelUrl = ollamaUrl + "/api/chat";
        Request.Builder okRequestBuilder = new Request.Builder().url(largeModelUrl);
        MediaType mediaType = MediaType.parse("application/x-ndjson");
        RequestBody requestBody = RequestBody.create(mediaType, bodyBytes);
        okRequestBuilder.post(requestBody);

        return SseTransformUtil.transformRequest(okRequestBuilder.build(), UUID.randomUUID().toString().replaceAll("-", ""));
    }


    // 向量召回
    public String callBackQuestion(String question) {
        //句子转向量
        EmbeddingReq embeddingReq = EmbeddingReq
                .builder()
                .model(embeddingModel)
                .prompt(question)
                .build();
        EmbeddingResult result = ollamaClient.embedding(embeddingReq);
        double[] vector = result.getEmbedding();

        // 向量召回
        String collection = elasticSearchStorage.getCollectionName();
        return elasticSearchStorage.retrieval(collection, vector);
    }
}
