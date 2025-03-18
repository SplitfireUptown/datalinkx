package com.datalinkx.driver.dsdriver;

import com.datalinkx.driver.model.DataTransJobDetail;

public interface IStreamDriver extends IDsDriver {
    Object getReaderInfo(DataTransJobDetail.Reader reader);
    Object getWriterInfo(DataTransJobDetail.Writer writer);
}
