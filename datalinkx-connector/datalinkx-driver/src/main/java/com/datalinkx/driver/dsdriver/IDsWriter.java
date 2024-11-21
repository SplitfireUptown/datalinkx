package com.datalinkx.driver.dsdriver;


import com.datalinkx.compute.connector.jdbc.PluginNode;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;

public interface IDsWriter extends IDsDriver {
    // ============= Flink引擎
    void truncateData(FlinkActionMeta param) throws Exception;
    Object getWriterInfo(FlinkActionMeta param) throws Exception;

    // ============= Seatunnel引擎
    // 构造seatunnel引擎写信息
    default PluginNode getSinkInfo(DataTransJobDetail.Writer writer) {
        return null;
    }
    // 构造seatunnel引擎sink中sql
    default String transferSinkSQL(DataTransJobDetail.Writer writer) {
        return "";
    }
}
