package com.datalinkx.dataserver.service.setupgenerator;

import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.driver.dsdriver.mysqldriver.MysqlSetupInfo;

public class MysqlSetupInfoGenerator implements SetupInfoGenerator<MysqlSetupInfo> {


    @Override
    public MysqlSetupInfo generateSetupInfo(DsBean dsBean) {
        MysqlSetupInfo mysqlSetupInfo = new MysqlSetupInfo();
        mysqlSetupInfo.setServer(dsBean.getHost());
        mysqlSetupInfo.setPort(dsBean.getPort());
        mysqlSetupInfo.setType("mysql");
        mysqlSetupInfo.setUid(dsBean.getUsername());
        mysqlSetupInfo.setPwd(dsBean.getPassword());
        mysqlSetupInfo.setDatabase(dsBean.getDatabase());
        return mysqlSetupInfo;
    }
}
