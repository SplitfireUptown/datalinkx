package com.datalinkx.dataserver.service.setupgenerator;

import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.driver.dsdriver.kafkadriver.KafkaSetupInfo;

public class KafkaSetupInfoGenerator implements SetupInfoGenerator<KafkaSetupInfo> {

    @Override
    public KafkaSetupInfo generateSetupInfo(DsBean dsBean) {
        KafkaSetupInfo kafkaSetupInfo = new KafkaSetupInfo();
        kafkaSetupInfo.setServer(dsBean.getHost());
        kafkaSetupInfo.setPort(dsBean.getPort());
        kafkaSetupInfo.setType("kafka");
        return kafkaSetupInfo;
    }
}
