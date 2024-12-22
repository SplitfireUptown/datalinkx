package com.datalinkx.compute.connector.http;

import com.datalinkx.compute.connector.jdbc.TransformNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/12/20 21:06
 */

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class HttpSource extends TransformNode {
    private String url;
    private String method;
    @Builder.Default
    private String format = "json";
//    @JsonProperty("content_field")
//    private String contentField;
    @JsonProperty("json_field")
    private Map<String, String> jsonField;
    private Map<String, Object> params;
    private Map<String, Object> headers;
    private Schema schema;
    private String body;
    private Pageing pageing;

    @Data
    public static final class Schema {
        // 为什么用LinkedHashMap？ 因为要保证写入顺序与页面上配置的字段映射顺序一致
        private LinkedHashMap<String, String> fields;
    }

    @Data
    public static final class Pageing {
        @JsonProperty("page_field")
        private String pageField;
        @JsonProperty("start_page_number")
        private Integer startPageNumber;
        @JsonProperty("batch_size")
        private Integer batchSize;
        @JsonProperty("total_page_size")
        private Integer totalPageSize;
    }
}
