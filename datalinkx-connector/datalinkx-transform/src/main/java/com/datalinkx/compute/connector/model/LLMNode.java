package com.datalinkx.compute.connector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/10/27 18:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
public class LLMNode extends TransformNode {

    @JsonProperty("model_provider")
    private String modelProvider;
    private String model;
    @JsonProperty("api_key")
    private String apiKey;
    private String prompt;
    @JsonProperty("openai.api_path")
    private String openaiApiPath;
    @JsonProperty("custom_config")
    private CustomConfig customConfig;

    @Data
    @Builder
    public static class CustomConfig {
        @JsonProperty("custom_response_parse")
        private String customResponseParse;
        @JsonProperty("custom_request_body")
        private customRequestBody customRequestBody;
    }

    @Data
    @Builder
    public static class customRequestBody {
        @Builder.Default
        private String model = "${model}";
        @Builder.Default
        private List<Message> message = new ArrayList<Message>() {{
            add(Message.builder().build());
        }};
    }

    @Data
    @Builder
    public static class Message {
        @Builder.Default
        private String role = "system";
        @Builder.Default
        private String content = "${prompt}";
    }
}
