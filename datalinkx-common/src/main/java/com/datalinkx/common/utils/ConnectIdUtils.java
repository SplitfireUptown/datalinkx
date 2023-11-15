package com.datalinkx.common.utils;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ConnectIdUtils {

    private ConnectIdUtils() { }

    public static String encodeConnectId(String s) {
        try {
            Map<String, Object> setUp = JsonUtils.json2Map(s);
            String setupStr = JsonUtils.toJson(new TreeMap<>(setUp));

            return GzipUtils.compress2Str(setupStr, "+-");
        } catch (Exception e) {
            log.error("encodeConnectId", e);
        }

        return null;
    }

    public static String decodeConnectId(String s) {
        try {
            return GzipUtils.uncompress2Str(s, "+-");
        } catch (IOException e) {
            log.error("decodeConnectId failed", e);
        }

        return "{}";
    }

    public static String getDsType(String connectId) {
        return JsonUtils.json2Map(decodeConnectId(connectId)).get("type").toString();
    }
}
