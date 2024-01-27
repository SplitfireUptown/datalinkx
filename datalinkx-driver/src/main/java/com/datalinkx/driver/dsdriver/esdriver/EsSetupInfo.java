package com.datalinkx.driver.dsdriver.esdriver;

import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsSetupInfo extends SetupInfo {
    private String address;
    private String uid;
    private String pwd;
}
