package com.datalinkx.dataserver.service.setupgenerator;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.driver.dsdriver.setupinfo.MysqlSetupInfo;

public class MySQLSetupInfoGenerator implements SetupInfoGenerator<MysqlSetupInfo> {


    @Override
    public MysqlSetupInfo generateSetupInfo(DsBean dsBean) {
        MysqlSetupInfo mysqlSetupInfo = new MysqlSetupInfo();
        mysqlSetupInfo.setServer(dsBean.getHost());
        mysqlSetupInfo.setPort(dsBean.getPort());
        mysqlSetupInfo.setType(MetaConstants.DsType.DS_MYSQL);
        mysqlSetupInfo.setUid(dsBean.getUsername());
        mysqlSetupInfo.setPwd(dsBean.getPassword());
        mysqlSetupInfo.setDatabase(dsBean.getDatabase());
        return mysqlSetupInfo;
    }
}
