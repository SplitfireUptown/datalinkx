package com.datalinkx.driver.dsdriver.kafkadriver;

import com.datalinkx.driver.dsdriver.IStreamDriver;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;

public class KafkaDriver implements AbstractDriver<KafkaSetupInfo, KafkaReader, KafkaWriter>, IStreamDriver {

    @Override
    public Object connect(boolean check) throws Exception {
        return null;
    }

    @Override
    public Object getReaderInfo(FlinkActionMeta param) throws Exception {
        return null;
    }

    @Override
    public Object getWriterInfo(FlinkActionMeta param) throws Exception {
        return null;
    }
}
