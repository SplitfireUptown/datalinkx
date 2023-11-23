package com.datalinkx.dataserver.config;


import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "datasource")
@Data
public class DsProperties {
    private List<String> importDs;
    private List<String> exportDs;
    private Boolean checkDuplicate = true;
    // for restapi
    boolean useRestapi = false;
    private String accessToken;
    private String method;
    private String url;
}
