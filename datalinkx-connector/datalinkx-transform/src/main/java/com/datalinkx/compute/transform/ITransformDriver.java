package com.datalinkx.compute.transform;


import com.datalinkx.compute.connector.model.TransformNode;
import com.fasterxml.jackson.databind.JsonNode;

public interface ITransformDriver {
    TransformNode transferInfo(String transferMeta);

    String analysisTransferMeta(JsonNode nodeMeta);
}
