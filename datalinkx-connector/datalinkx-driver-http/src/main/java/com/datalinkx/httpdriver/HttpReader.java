package com.datalinkx.httpdriver;

import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;


@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class HttpReader extends AbstractReader {
    private String url;
    private String method;
    private String format = "json";
    private Pageing timeout;
    private Map<String, String> params;
    private String contentField;
    private Map<String, Map<String, String>> schema = new HashMap<String, Map<String, String>>() {{
        put("fields", fields);
    }};
    private Map<String, String> fields = new HashMap<>();

    @JsonIgnoreProperties
    @Data
    public static class Pageing {
        @JsonProperty("total_page_size")
        private Integer totalPageSize;
        @JsonProperty("page_field")
        private String pageField;
    }
}
