package com.datalinkx.dataserver.service.setupgenerator;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.driver.dsdriver.setupinfo.RedisSetupInfo;
import org.springframework.util.StringUtils;

public class RedisSetupInfoGenerator implements SetupInfoGenerator<RedisSetupInfo> {

    @Override
    public RedisSetupInfo generateSetupInfo(DsBean dsBean) {
        RedisSetupInfo redisSetupInfo = new RedisSetupInfo();
        redisSetupInfo.setDatabase(Integer.parseInt(StringUtils.hasLength(dsBean.getDatabase()) ? dsBean.getDatabase() : "0"));
        redisSetupInfo.setHost(dsBean.getHost());
        redisSetupInfo.setPort(dsBean.getPort());
        redisSetupInfo.setPwd(dsBean.getPassword());
        redisSetupInfo.setType(MetaConstants.DsType.DS_REDIS);
        return redisSetupInfo;
    }
}
