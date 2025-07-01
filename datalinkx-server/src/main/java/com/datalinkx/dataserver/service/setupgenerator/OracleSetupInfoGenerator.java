package com.datalinkx.dataserver.service.setupgenerator;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.driver.dsdriver.setupinfo.OracleSetupInfo;

import java.util.Map;

public class OracleSetupInfoGenerator implements SetupInfoGenerator<OracleSetupInfo> {


    @Override
    public OracleSetupInfo generateSetupInfo(DsBean dsBean) {
        OracleSetupInfo oracleSetupInfo = new OracleSetupInfo();
        oracleSetupInfo.setType(MetaConstants.DsType.DS_ORACLE);
        oracleSetupInfo.setServer(dsBean.getHost());
        oracleSetupInfo.setPort(dsBean.getPort());
        oracleSetupInfo.setPwd(dsBean.getPassword());
        oracleSetupInfo.setUid(dsBean.getUsername());

        Map configMap = JsonUtils.toObject(dsBean.getConfig(), Map.class);
        if (configMap.containsKey("sid")) {
            oracleSetupInfo.setConnectType("SID");
            oracleSetupInfo.setSid((String) configMap.get("sid"));
        } else {
            oracleSetupInfo.setConnectType("SERVERNAME");
            oracleSetupInfo.setSid((String) configMap.get("servername"));
        }
        return oracleSetupInfo;
    }
}
