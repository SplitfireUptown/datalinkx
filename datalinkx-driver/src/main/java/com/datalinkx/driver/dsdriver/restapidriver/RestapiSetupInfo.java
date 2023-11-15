package com.datalinkx.driver.dsdriver.restapidriver;

import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestapiSetupInfo extends SetupInfo {

    private String url;

    private String method;

    private String token;


}
