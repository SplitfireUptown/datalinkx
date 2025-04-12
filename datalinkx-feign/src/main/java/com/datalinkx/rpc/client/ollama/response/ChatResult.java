package com.datalinkx.rpc.client.ollama.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChatResult {
    private String model;
    @JsonProperty("created_at")
    private String createdAt;
    private Content message;
    @JsonProperty("done_reason")
    private String doneReason;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class Content {
        private String role;
        private String content;
    }
}
