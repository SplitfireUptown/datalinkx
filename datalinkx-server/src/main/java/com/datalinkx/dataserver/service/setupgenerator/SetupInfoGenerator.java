package com.datalinkx.dataserver.service.setupgenerator;

import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;

public interface SetupInfoGenerator<T extends SetupInfo> {

    T generateSetupInfo(DsBean dsBean);
}
