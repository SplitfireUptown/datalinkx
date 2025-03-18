package com.datalinkx.driver.dsdriver.base;


import com.datalinkx.common.utils.Base64Utils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.driver.dsdriver.base.connect.SetupInfo;
import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;
import lombok.SneakyThrows;

public interface AbstractDriver<T extends SetupInfo, P extends AbstractReader, Q extends AbstractWriter> {

    @SneakyThrows
    default String rebuildPassword(String password) {
        if (ObjectUtils.isEmpty(password)) {
            return "";
        }

       return new String(Base64Utils.decodeBase64(password));
    }
}
