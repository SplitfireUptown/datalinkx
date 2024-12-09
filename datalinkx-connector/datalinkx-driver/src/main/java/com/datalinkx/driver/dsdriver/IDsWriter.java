package com.datalinkx.driver.dsdriver;


import com.datalinkx.compute.connector.jdbc.TransformNode;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.model.SeatunnelActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;

public interface IDsWriter extends IDsDriver {
    // ============= Flinkx 引擎
    void truncateData(FlinkActionMeta param) throws Exception;
    Object getWriterInfo(FlinkActionMeta param) throws Exception;

    // ============= Seatunnel引擎
    // 构造seatunnel引擎写信息
    default TransformNode getSinkInfo(SeatunnelActionMeta param) {
        return null;
    }
    // 构造seatunnel引擎sink中sql
    default String transferSinkSQL(SeatunnelActionMeta param) {
        return "";
    }
}
