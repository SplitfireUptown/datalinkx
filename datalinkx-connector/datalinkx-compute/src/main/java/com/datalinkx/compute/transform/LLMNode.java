package com.datalinkx.compute.transform;

import com.datalinkx.compute.model.TransformNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/10/27 18:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class LLMNode extends TransformNode {
    @JsonProperty("source_table_name")
    private String sourceTableName;
    @JsonProperty("model_provider")
    private String modelProvider;
    private String model;
    @JsonProperty("api_key")
    private String apiKey;
    private String prompt;
    @JsonProperty("openai.api_path")
    private String openaiApiPath;
    @JsonProperty("custom_config")
    private Map<String, Object> customConfig;
}
