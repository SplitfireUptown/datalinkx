package com.datalinkx.driver.dsdriver;


import com.datalinkx.driver.dsdriver.base.model.FlinkActionParam;
import com.datalinkx.driver.dsdriver.base.model.TableId;

public interface IDsWriter extends IDsDriver {
    void truncateData(FlinkActionParam param) throws Exception;
    Object getWriterInfo(FlinkActionParam param);
    void afterWrite(FlinkActionParam param);
}
