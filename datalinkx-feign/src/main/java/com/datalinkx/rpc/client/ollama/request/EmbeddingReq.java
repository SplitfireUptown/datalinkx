package com.datalinkx.rpc.client.ollama.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmbeddingReq {
    private String model;
    private String prompt;
}
