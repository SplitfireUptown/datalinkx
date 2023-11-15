package com.datalinkx.common.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

public final class HttpUtil {
    public static final int PORT_80 = 80;
    public static final int PORT_443 = 443;
    public static final int SCHEMA_SUFFIX_LEN = 3;

    private HttpUtil() { }

    public static HttpMeta partHttp(String add) throws Exception {
        String text = add;
        String schema = null;
        int schemeIdx = add.indexOf("://");
        if (schemeIdx > 0) {
            schema = add.substring(0, schemeIdx);
            if (!"http".equals(schema) && !"https".equals(schema)) {
                throw new Exception("schema error");
            }
            text = add.substring(schemeIdx + SCHEMA_SUFFIX_LEN);
        }
        String[] pair = text.split(":", 2);
        int port;
        if (null != schema) {
            if (StringUtils.isEmpty(pair[1])) {
                if ("http".equals(schema)) {
                    port = PORT_80;
                } else {
                    port = PORT_443;
                }
            } else {
                try {
                    port = Integer.parseInt(pair[1]);
                } catch (Exception e) {
                    throw new Exception("port error");
                }
            }
        } else {
            if (StringUtils.isEmpty(pair[1])) {
                port = PORT_80;
            } else {
                port = Integer.parseInt(pair[1]);
            }
        }

        return new HttpMeta(pair[0], port, schema);
    }

    @Data
    @AllArgsConstructor
    public static class HttpMeta {
        String ip;
        int port;
        String schema;
    }
}
