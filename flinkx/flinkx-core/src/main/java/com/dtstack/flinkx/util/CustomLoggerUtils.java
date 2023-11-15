package com.dtstack.flinkx.util;

import java.util.HashMap;
import java.util.Map;

import com.dtstack.flinkx.constants.Metrics;
import org.apache.flink.api.common.functions.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class CustomLoggerUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CustomLoggerUtils.class);

    public static void setMdcProp(RuntimeContext runtimeContext) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        if (null == contextMap) {
            contextMap = new HashMap<>();
        }

        if (runtimeContext == null || runtimeContext.getMetricGroup() == null) {
            return;
        }

        Map<String, String> vars = runtimeContext.getMetricGroup().getAllVariables();
        if(vars != null && vars.get(Metrics.JOB_NAME) != null) {
            contextMap.put("flink_job_name", vars.get(Metrics.JOB_NAME));
        }

        if(vars!= null && vars.get(Metrics.JOB_ID) != null) {
            contextMap.put("flink_job_id", vars.get(Metrics.JOB_ID));
        }

//        LOG.info("==== " + GsonUtil.GSON.toJson(contextMap));

        MDC.setContextMap(contextMap);
    }
}
