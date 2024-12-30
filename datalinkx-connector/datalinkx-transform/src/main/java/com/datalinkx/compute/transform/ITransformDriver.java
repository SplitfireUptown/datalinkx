package com.datalinkx.compute.transform;


import com.datalinkx.compute.connector.jdbc.TransformNode;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

public abstract class ITransformDriver {
    public abstract TransformNode transferInfo(Map<String, Object> commonSettings, String meta);
    public abstract String analysisTransferMeta(JsonNode nodeMeta);
}
