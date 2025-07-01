package com.datalinkx.dataserver.service.setupgenerator;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.driver.dsdriver.setupinfo.MysqlcdcSetupInfo;

public class MySQLCDCInfoGenerator implements SetupInfoGenerator<MysqlcdcSetupInfo> {


    @Override
    public MysqlcdcSetupInfo generateSetupInfo(DsBean dsBean) {
        MysqlcdcSetupInfo mysqlcdcSetupInfo = new MysqlcdcSetupInfo();
        mysqlcdcSetupInfo.setServer(dsBean.getHost());
        mysqlcdcSetupInfo.setPort(dsBean.getPort());
        mysqlcdcSetupInfo.setType(MetaConstants.DsType.DS_MYSQLCDC);
        mysqlcdcSetupInfo.setUid(dsBean.getUsername());
        mysqlcdcSetupInfo.setPwd(dsBean.getPassword());
        mysqlcdcSetupInfo.setDatabase(dsBean.getDatabase());
        return mysqlcdcSetupInfo;
    }
}
