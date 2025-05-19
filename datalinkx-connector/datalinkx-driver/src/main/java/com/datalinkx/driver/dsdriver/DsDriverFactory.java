package com.datalinkx.driver.dsdriver;

import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JarLoaderUtil;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;


@Slf4j
public final class DsDriverFactory {

    private DsDriverFactory() {

    }
    private static final String PACKAGE_PREFIX = "com.datalinkx.";
    private static final String DRIVER_DIST = "driver-dist";

    private static String getDriverClass(String driverName) {
        return PACKAGE_PREFIX + driverName.toLowerCase() + "driver" + "." + ConnectIdUtils.toPascalCase(driverName) + "Driver";
    }

    public static IDsDriver getDriver(String connectId) throws Exception {
        String dsType = ConnectIdUtils.getDsType(connectId);
        // 假设 JarLoaderUtil.loadJarsFromDirectory 加载 JAR 包到系统类加载器，返回 URL 列表
        List<URL> dataSourcePlugins = JarLoaderUtil.loadJarsFromDirectory(DRIVER_DIST);
        String targetJarName = String.format("datalinkx-driver-%s-1.0.0.jar", dsType.toLowerCase());
        String targetClassName = getDriverClass(dsType);

        URL targetJarUrl = dataSourcePlugins.stream()
                .filter(plugin -> plugin.getPath().endsWith(targetJarName))
                .findFirst()
                .orElseThrow(() -> new DatalinkXServerException(StatusCode.DRIVER_LOAD_FAIL));

        // 使用自定义的 URLClassLoader 加载目标 JAR 包中的类
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{targetJarUrl}, Thread.currentThread().getContextClassLoader());
        Class<?> clazz = Class.forName(targetClassName, true, urlClassLoader);

        Constructor<?> constructor = clazz.getConstructor(String.class);
        return (IDsDriver) constructor.newInstance(connectId);
    }

    public static IStreamDriver getStreamDriver(String connectId) throws Exception {
//        String dsType = ConnectIdUtils.getDsType(connectId);
//        String driverClassName = getDriverClass(dsType);
//        Class<?> driverClass = Class.forName(driverClassName);
//        Constructor<?> constructor = driverClass.getDeclaredConstructor(String.class);
//        return (IStreamDriver) constructor.newInstance(connectId);
        try {
            try {
                return (IStreamDriver) getDriver(connectId);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("driver load error", e);
            }
        } catch (NoSuchMethodException e) {
            log.error("driver load error", e);
        }

        throw new Exception("can not initialize driver");
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
