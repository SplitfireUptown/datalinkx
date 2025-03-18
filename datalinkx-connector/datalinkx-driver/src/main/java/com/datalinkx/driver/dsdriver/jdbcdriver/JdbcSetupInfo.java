package com.datalinkx.driver.dsdriver.jdbcdriver;

import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JdbcSetupInfo extends SetupInfo {
    private String database;
    private String server;
    private String pwd;
    private String uid;
    private Integer port;
}
