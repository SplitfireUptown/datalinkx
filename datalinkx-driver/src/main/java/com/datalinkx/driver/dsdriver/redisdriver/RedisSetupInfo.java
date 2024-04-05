package com.datalinkx.driver.dsdriver.redisdriver;

import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RedisSetupInfo extends SetupInfo {
    private String host;
    private String pwd;
    private Integer port;
    private Integer database;
}
