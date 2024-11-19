package com.datalinkx.driver.dsdriver;


import com.datalinkx.compute.connector.jdbc.PluginNode;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;

public interface IDsWriter extends IDsDriver {
    void truncateData(FlinkActionMeta param) throws Exception;
    Object getWriterInfo(FlinkActionMeta param) throws Exception;
    default PluginNode getSinkInfo(DataTransJobDetail.Writer writer) {
        return null;
    }

}
