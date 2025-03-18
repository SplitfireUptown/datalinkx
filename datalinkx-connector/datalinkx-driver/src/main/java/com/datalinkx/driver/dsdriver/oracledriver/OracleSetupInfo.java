package com.datalinkx.driver.dsdriver.oracledriver;

import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcSetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;


@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OracleSetupInfo extends JdbcSetupInfo {
    private String connectType;
    private String sid;
}
