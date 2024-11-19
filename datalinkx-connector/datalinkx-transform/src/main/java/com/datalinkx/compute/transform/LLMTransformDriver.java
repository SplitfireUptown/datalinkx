package com.datalinkx.compute.transform;

import com.datalinkx.compute.connector.model.TransformNode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * @author: uptown
 * @date: 2024/11/17 17:36
 */
@Data
public class LLMTransformDriver implements ITransformDriver {
    public LLMTransformDriver() {

    }

    @Override
    public TransformNode transferInfo(String transferMeta) {
        return null;
    }

    @Override
    public String analysisTransferMeta(JsonNode nodeMeta) {
        return null;
    }
}
