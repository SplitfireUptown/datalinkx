package com.datalinkx.driver.dsdriver;

import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;

public interface IStreamDriver extends IDsDriver {
    Object getReaderInfo(FlinkActionMeta param);
    Object getWriterInfo(FlinkActionMeta param);
}
