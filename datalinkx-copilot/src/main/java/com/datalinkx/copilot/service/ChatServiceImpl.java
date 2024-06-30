package com.datalinkx.copilot.service;

import java.util.Collections;

import com.datalinkx.copilot.client.OllamaClient;
import com.datalinkx.copilot.client.request.ChatReq;
import com.datalinkx.copilot.client.request.EmbeddingReq;
import com.datalinkx.copilot.client.response.ChatResult;
import com.datalinkx.copilot.client.response.EmbeddingResult;
import com.datalinkx.copilot.llm.LLMUtils;
import com.datalinkx.copilot.vector.ElasticSearchVectorStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    OllamaClient ollamaClient;

    @Autowired
    ElasticSearchVectorStorage elasticSearchStorage;

    @Value("${llm.embedding}")
    String embeddingModel;
    @Value("${llm.model}")
    String chatModel;

    @Override
    public String chat(String question) {
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
        String vectorData = elasticSearchStorage.retrieval(collection, vector);
        // 构建Prompt
        String prompt = LLMUtils.buildPrompt(question, vectorData);
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
}
