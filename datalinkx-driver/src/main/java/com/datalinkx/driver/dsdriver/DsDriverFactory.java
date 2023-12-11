package com.datalinkx.driver.dsdriver;

import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.driver.dsdriver.esdriver.EsDriver;
import com.datalinkx.driver.dsdriver.mysqldriver.MysqlDriver;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public final class  DsDriverFactory {

    private DsDriverFactory() {

    }
    private static Map<String, Class> dsDriverMap = new ConcurrentHashMap<>();
    static {
        dsDriverMap.put("elasticsearch", EsDriver.class);
        dsDriverMap.put("mysql", MysqlDriver.class);
    }

    public static IDsReader getDsReader(String connectId) throws Exception {
        String dsType = ConnectIdUtils.getDsType(connectId);
        Class clazz = dsDriverMap.get(dsType.toLowerCase());
        try {
            Constructor constructor = clazz.getDeclaredConstructor(String.class);
            try {
                IDsReader dsReader = (IDsReader) constructor.newInstance(connectId);
                return dsReader;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("create ds dsdriver error", e);
            }
        } catch (NoSuchMethodException e) {
            log.error("create ds dsdriver error", e);
        }

        throw new Exception("no dsdriver");
    }

    public static IDsWriter getDsWriter(String connectId) throws Exception {
        String dsType = ConnectIdUtils.getDsType(connectId);
        Class clazz = dsDriverMap.get(dsType);
        try {
            Constructor constructor = clazz.getDeclaredConstructor(String.class);
            try {
                IDsWriter dsWriter = (IDsWriter) constructor.newInstance(connectId);
                return dsWriter;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("create ds dsdriver error", e);
            }
        } catch (NoSuchMethodException e) {
            log.error("create ds dsdriver error", e);
        }

        throw new Exception("no dsdriver");
    }
}
