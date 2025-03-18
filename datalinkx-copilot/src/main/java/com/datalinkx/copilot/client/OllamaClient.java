package com.datalinkx.copilot.client;

import com.datalinkx.copilot.client.request.ChatReq;
import com.datalinkx.copilot.client.request.EmbeddingReq;
import com.datalinkx.copilot.client.response.ChatResult;
import com.datalinkx.copilot.client.response.EmbeddingResult;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OllamaClient {

    @POST("/api/embeddings")
    EmbeddingResult embedding(@Body EmbeddingReq embeddingReq);

    @POST("/api/chat")
    ChatResult chat(@Body ChatReq chatReq);
}
