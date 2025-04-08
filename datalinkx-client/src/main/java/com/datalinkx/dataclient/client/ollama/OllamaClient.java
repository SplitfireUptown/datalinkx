package com.datalinkx.dataclient.client.ollama;

import com.datalinkx.dataclient.client.ollama.request.ChatReq;
import com.datalinkx.dataclient.client.ollama.request.EmbeddingReq;
import com.datalinkx.dataclient.client.ollama.response.ChatResult;
import com.datalinkx.dataclient.client.ollama.response.EmbeddingResult;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface OllamaClient {

    @POST("/api/embeddings")
    EmbeddingResult embedding(@Body EmbeddingReq embeddingReq);

    @POST("/api/chat")
    ChatResult chat(@Body ChatReq chatReq);
}
