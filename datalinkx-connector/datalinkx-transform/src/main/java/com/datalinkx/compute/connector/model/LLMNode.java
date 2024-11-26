package com.datalinkx.compute.connector.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/10/27 18:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
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
    public static class CustomConfig {
        @JsonProperty("custom_response_parse")
        private String customResponseParse;
        @JsonProperty("custom_request_body")
        private customRequestBody customRequestBody;
    }

    @Data
    public static class customRequestBody {
        private String model;
        private List<Message> message = new ArrayList<Message>() {{
            add(new Message());
        }};
    }

    @Data
    public static class Message {
        private String role = "system";
        private String content = "${prompt}";
    }
}
