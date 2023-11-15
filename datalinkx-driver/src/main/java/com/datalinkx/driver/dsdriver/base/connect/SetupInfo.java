package com.datalinkx.driver.dsdriver.base.connect;

import lombok.Data;

@Data
public class SetupInfo {
    String type;
    private Boolean crypter;
    private Integer isExport = 0;
}
