package com.datalinkx.dataserver.service.setupgenerator;

import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.driver.dsdriver.setupinfo.CustomSetupInfo;

public class CustomSetupInfoGenerator implements SetupInfoGenerator<CustomSetupInfo> {

    @Override
    public CustomSetupInfo generateSetupInfo(DsBean dsBean) {
        CustomSetupInfo customSetupInfo = new CustomSetupInfo();
        customSetupInfo.setConfig(dsBean.getConfig());
        customSetupInfo.setType(dsBean.getType());
        return customSetupInfo;
    }
}
