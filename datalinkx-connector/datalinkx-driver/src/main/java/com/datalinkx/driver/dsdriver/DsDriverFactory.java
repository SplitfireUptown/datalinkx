package com.datalinkx.driver.dsdriver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.datalinkx.common.utils.ConnectIdUtils;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class DsDriverFactory {

    private DsDriverFactory() {

    }
    private static final String PACKAGE_PREFIX = "com.datalinkx.driver.dsdriver.";

    private static String getDriverClass(String driverName) {
        return PACKAGE_PREFIX + driverName.toLowerCase() + "driver" + "." + ConnectIdUtils.toPascalCase(driverName) + "Driver";
    }

    public static IDsDriver getDriver(String connectId) throws Exception {
        String dsType = ConnectIdUtils.getDsType(connectId);
        String driverClassName = getDriverClass(dsType);
        Class<?> driverClass = Class.forName(driverClassName);
        Constructor<?> constructor = driverClass.getDeclaredConstructor(String.class);
        return (IDsDriver) constructor.newInstance(connectId);
    }

    public static IStreamDriver getStreamDriver(String connectId) throws Exception {
        String dsType = ConnectIdUtils.getDsType(connectId);
        String driverClassName = getDriverClass(dsType);
        Class<?> driverClass = Class.forName(driverClassName);
        Constructor<?> constructor = driverClass.getDeclaredConstructor(String.class);
        return (IStreamDriver) constructor.newInstance(connectId);
    }

    public static IDsReader getDsReader(String connectId) throws Exception {
        try {
            try {
                return (IDsReader) getDriver(connectId);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("driver load error", e);
            }
        } catch (NoSuchMethodException e) {
            log.error("driver load error", e);
        }

        throw new Exception("can not initialize driver");
    }

    public static IDsWriter getDsWriter(String connectId) throws Exception {
        try {
            try {
                return (IDsWriter) getDriver(connectId);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("driver load error", e);
            }
        } catch (NoSuchMethodException e) {
            log.error("driver load error", e);
        }

        throw new Exception("can not initialize driver");
    }
}
