package com.datalinkx.driver.dsdriver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.driver.dsdriver.esdriver.EsDriver;
import com.datalinkx.driver.dsdriver.mysqldriver.MysqlDriver;
import com.datalinkx.driver.dsdriver.oracledriver.OracleDriver;
import com.datalinkx.driver.dsdriver.redisDriver.RedisDriver;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class  DsDriverFactory {

    private DsDriverFactory() {

    }
    private static Map<String, Class> dsDriverMap = new ConcurrentHashMap<>();
    static {
        dsDriverMap.put("elasticsearch", EsDriver.class);
        dsDriverMap.put("mysql", MysqlDriver.class);
        dsDriverMap.put("oracle", OracleDriver.class);
        dsDriverMap.put("redis", RedisDriver.class);
    }

    public static IDsDriver getDriver(String connectId) throws Exception {
        String dsType = ConnectIdUtils.getDsType(connectId);
        Class clazz = dsDriverMap.get(dsType.toLowerCase());
        Constructor constructor = clazz.getDeclaredConstructor(String.class);
        return (IDsDriver) constructor.newInstance(connectId);
    }

    public static IDsReader getDsReader(String connectId) throws Exception {
        String dsType = ConnectIdUtils.getDsType(connectId);
        Class clazz = dsDriverMap.get(dsType.toLowerCase());
        try {
            Constructor constructor = clazz.getDeclaredConstructor(String.class);
            try {
                return (IDsReader) constructor.newInstance(connectId);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("driver load error", e);
            }
        } catch (NoSuchMethodException e) {
            log.error("driver load error", e);
        }

        throw new Exception("can not initialize driver");
    }

    public static IDsWriter getDsWriter(String connectId) throws Exception {
        String dsType = ConnectIdUtils.getDsType(connectId);
        Class clazz = dsDriverMap.get(dsType);
        try {
            Constructor constructor = clazz.getDeclaredConstructor(String.class);
            try {
                return (IDsWriter) constructor.newInstance(connectId);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("driver load error", e);
            }
        } catch (NoSuchMethodException e) {
            log.error("driver load error", e);
        }

        throw new Exception("can not initialize driver");
    }
}
