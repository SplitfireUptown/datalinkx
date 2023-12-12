package com.datalinkx.datajob.config;


import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "support")
@Data
public class DsProperties {
    private List<String> datasource;
}
