package com.datalinkx.driver.dsdriver.setupinfo;

import com.datalinkx.driver.dsdriver.base.meta.SetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsSetupInfo extends SetupInfo {
    private String address;
    private String uid;
    private String pwd;
}
