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

    public static String toPascalCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c) || c == '_' || c == '-') {
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }

        return result.toString();
    }
}
