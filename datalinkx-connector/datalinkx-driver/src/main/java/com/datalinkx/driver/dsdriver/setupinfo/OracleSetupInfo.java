package com.datalinkx.driver.dsdriver.setupinfo;

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
