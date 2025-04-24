package com.datalinkx.driver.dsdriver;


import com.datalinkx.common.result.DatalinkXJobDetail;
import com.datalinkx.compute.connector.jdbc.TransformNode;

public interface IDsWriter extends IDsDriver {
    // ============= Flinkx 引擎
    void truncateData(DatalinkXJobDetail.Writer writer) throws Exception;
    Object getWriterInfo(DatalinkXJobDetail.Writer writer) throws Exception;

    // ============= Seatunnel引擎
    // 构造seatunnel引擎写信息
    default TransformNode getSinkInfo(DatalinkXJobDetail.Writer writer) {
        return null;
    }
    // 构造seatunnel引擎sink中sql
    default String transferSinkSQL(DatalinkXJobDetail.Writer writer) {
        return "";
    }
}
