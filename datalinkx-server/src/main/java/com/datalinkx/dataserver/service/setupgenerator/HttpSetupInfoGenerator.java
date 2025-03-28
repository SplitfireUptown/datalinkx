package com.datalinkx.dataserver.service.setupgenerator;

import cn.hutool.core.lang.Pair;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.client.HttpConstructor;
import com.datalinkx.driver.dsdriver.httpdriver.HttpSetupInfo;

public class HttpSetupInfoGenerator implements SetupInfoGenerator<HttpSetupInfo> {

    @Override
    public HttpSetupInfo generateSetupInfo(DsBean dsBean) {
        HttpSetupInfo httpSetupInfo = JsonUtils.toObject(dsBean.getConfig(), HttpSetupInfo.class);
        Pair<String, Integer> host = HttpConstructor.checkUrlFormat(httpSetupInfo.getUrl());
        httpSetupInfo.setHost(host.getKey());
        httpSetupInfo.setPort(host.getValue());
        httpSetupInfo.setType("http");
        return httpSetupInfo;
    }
}
