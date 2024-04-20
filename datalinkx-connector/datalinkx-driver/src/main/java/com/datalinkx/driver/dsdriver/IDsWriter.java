package com.datalinkx.driver.dsdriver;


import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;

public interface IDsWriter extends IDsDriver {
    void truncateData(FlinkActionMeta param) throws Exception;
    Object getWriterInfo(FlinkActionMeta param) throws Exception;
    void afterWrite(FlinkActionMeta param);
}
