package com.datalinkx.driver.dsdriver;


import com.datalinkx.common.result.DatalinkXJobDetail;

public interface IStreamDriver extends IDsDriver {
    Object getReaderInfo(DatalinkXJobDetail.Reader reader);
    Object getWriterInfo(DatalinkXJobDetail.Writer writer);
}
