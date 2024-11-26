package com.datalinkx.compute.transform;

import com.datalinkx.compute.connector.model.LLMNode;
import com.datalinkx.compute.connector.model.TransformNode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/11/17 17:36
 */
@Slf4j
public class LLMTransformDriver extends ITransformDriver {


    @Override
    public TransformNode transferInfo(Map<String, Object> commonSettings, String meta) {
        return LLMNode.builder().build();
    }

    @Override
    public String analysisTransferMeta(JsonNode nodeMeta) {
        return null;
    }
}
