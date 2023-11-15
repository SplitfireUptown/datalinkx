package com.datalinkx.dataio.config;


import java.util.List;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author uptown
 * @Description TODO
 * @createTime 2021年05月27日 10:59:01
 */
@Component
@ConfigurationProperties(prefix = "support")
@Data
public class DsProperties {
    private List<String> datasource;
}
