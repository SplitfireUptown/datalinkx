package com.datalinkx.driver.dsdriver.base;


import com.datalinkx.common.utils.Base64Utils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.driver.dsdriver.base.meta.SetupInfo;
import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.net.URLClassLoader;

public abstract class AbstractDriver<T extends SetupInfo, P extends AbstractReader, Q extends AbstractWriter> {

    @Setter
    protected URLClassLoader urlClassLoader;

    @SneakyThrows
    public String rebuildPassword(String password) {
        if (ObjectUtils.isEmpty(password)) {
            return "";
        }

       return new String(Base64Utils.decodeBase64(password));
    }

}
