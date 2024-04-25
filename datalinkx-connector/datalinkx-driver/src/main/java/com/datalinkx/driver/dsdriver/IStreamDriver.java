package com.datalinkx.driver.dsdriver;

import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;

public interface IStreamDriver {
    Object connect(boolean check) throws Exception;
    Object getReaderInfo(FlinkActionMeta param);
    Object getWriterInfo(FlinkActionMeta param);
}
