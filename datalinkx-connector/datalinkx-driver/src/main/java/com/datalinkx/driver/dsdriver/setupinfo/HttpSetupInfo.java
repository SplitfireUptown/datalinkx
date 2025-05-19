package com.datalinkx.driver.dsdriver.setupinfo;

import com.datalinkx.driver.dsdriver.base.meta.SetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author: uptown
 * @date: 2024/12/17 21:46
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HttpSetupInfo extends SetupInfo {
    private String url;
    private String method;
    private String host;
    private Integer port;
    @JsonProperty("content_type")
    private String contentType;
    private List<ItemConfig> header;
    private List<ItemConfig> param;
    private List<ItemConfig> body;
    private String raw;
    @JsonProperty("json_path")
    private String jsonPath;
    @JsonProperty("rev_data")
    private String revData;


    @Data
    public static final class ItemConfig {
        private String key;
        private String value;
    }
}
