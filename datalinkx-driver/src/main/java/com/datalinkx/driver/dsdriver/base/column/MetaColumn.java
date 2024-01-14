package com.datalinkx.driver.dsdriver.base.column;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetaColumn {
    private static Map<String, String> dbColumnTypeMap = new HashMap<>();
    public static Map<String, String> esColumnTypeMap = new HashMap<>();

    static {
        dbColumnTypeMap.put("string", "text");
        dbColumnTypeMap.put("number", "bigint");
        dbColumnTypeMap.put("double", "double");
        dbColumnTypeMap.put("date", "date");

        esColumnTypeMap.put("string", "keyword");
        esColumnTypeMap.put("number", "long");
        esColumnTypeMap.put("double", "double");
        esColumnTypeMap.put("date", "date");
    }

    public static Map<String, String> getDbColumnTypeMap() {
        return dbColumnTypeMap;
    }

    private String name;
    private String type;
    private String value;
    private String format;
}
