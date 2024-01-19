package com.datalinkx.driver.dsdriver;


import com.datalinkx.driver.dsdriver.base.model.FlinkActionParam;

public interface IDsWriter extends IDsDriver {
    void truncateData(FlinkActionParam param) throws Exception;
    Object getWriterInfo(FlinkActionParam param) throws Exception;
    void afterWrite(FlinkActionParam param);
}
