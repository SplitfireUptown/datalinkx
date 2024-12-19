package com.datalinkx.driver.dsdriver.httpdriver;

import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

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
    private List<ItemConfig> header;
    private List<ItemConfig> param;
    private List<ItemConfig> body;
    private String raw;


    @Data
    public static final class ItemConfig {
        private String key;
        private String value;
    }
}
