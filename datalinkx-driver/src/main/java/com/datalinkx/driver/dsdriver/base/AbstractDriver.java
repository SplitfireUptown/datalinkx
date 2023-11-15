package com.datalinkx.driver.dsdriver.base;


import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;

public interface AbstractDriver<T extends SetupInfo, P extends AbstractReader, Q extends AbstractWriter> {

}
