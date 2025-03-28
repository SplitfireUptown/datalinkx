package com.datalinkx.dataserver.service.setupgenerator;

import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.datalinkx.driver.dsdriver.esdriver.EsSetupInfo;

public class EsSetupInfoGenerator implements SetupInfoGenerator<EsSetupInfo> {

    @Override
    public EsSetupInfo generateSetupInfo(DsBean dsBean) {
        EsSetupInfo esSetupInfo = new EsSetupInfo();
        esSetupInfo.setType("elasticsearch");
        esSetupInfo.setAddress(dsBean.getHost() + ":" + dsBean.getPort());
        esSetupInfo.setPwd(dsBean.getPassword());
        esSetupInfo.setUid(dsBean.getUsername());
        return esSetupInfo;
    }
}
