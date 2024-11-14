package com.datalinkx.driver.dsdriver;

import com.datalinkx.driver.model.DataTransJobDetail;

public interface IComputeDriver extends IDsDriver {
    Object getSourceInfo(DataTransJobDetail.Reader reader);
    Object getSinkInfo(DataTransJobDetail.Writer writer);
}
