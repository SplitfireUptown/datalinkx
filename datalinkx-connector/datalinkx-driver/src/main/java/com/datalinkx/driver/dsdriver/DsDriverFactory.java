package com.datalinkx.driver.dsdriver;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JarLoaderUtil;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.setupinfo.CustomSetupInfo;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Map;
import java.util.Objects;


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
        String setupInfo = ConnectIdUtils.decodeConnectId(connectId);
        Map setupInfoMap = JsonUtils.toObject(setupInfo, Map.class);
        String type = (String) setupInfoMap.get("type");
        String dsType = (String) setupInfoMap.get("type");

        // 如果是自定义数据源，取setup中的type
        if (Objects.equals(type, MetaConstants.DsType.DS_CUSTOM)) {
            CustomSetupInfo customSetupInfo = JsonUtils.toObject(setupInfo, CustomSetupInfo.class);
            Map customDsConfig = JsonUtils.toObject(customSetupInfo.getConfig(), Map.class);
            dsType = (String) customDsConfig.get("type");
        }

        List<URL> dataSourcePlugins = JarLoaderUtil.loadJarsFromDirectory(DRIVER_DIST);
        String targetJarName = String.format("datalinkx-driver-%s-1.0.0.jar", dsType.toLowerCase());
        String targetClassName = getDriverClass(dsType);

        URL targetJarUrl = dataSourcePlugins.stream()
                .filter(plugin -> plugin.getPath().endsWith(targetJarName))
                .findFirst()
                .orElseThrow(() -> new DatalinkXServerException(StatusCode.DRIVER_LOAD_FAIL, "系统未支持该数据源类型"));

        // 使用自定义的 URLClassLoader 加载目标 JAR 包中的类
        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{targetJarUrl}, Thread.currentThread().getContextClassLoader());
        Class<?> clazz = Class.forName(targetClassName, true, urlClassLoader);

        Constructor<?> constructor = clazz.getConstructor(String.class);
        Object driver = constructor.newInstance(connectId);
        AbstractDriver abstractDriver = (AbstractDriver) driver;
        abstractDriver.setUrlClassLoader(urlClassLoader);

        return (IDsDriver) driver;
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
                log.error("驱动加载异常", e);
            }
        } catch (NoSuchMethodException e) {
            log.error("系统未支持该数据源类型", e);
        }

        throw new Exception("系统未支持该数据源类型");
    }

    public static IDsWriter getDsWriter(String connectId) throws Exception {
        try {
            try {
                return (IDsWriter) getDriver(connectId);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                log.error("驱动加载异常", e);
            }
        } catch (NoSuchMethodException e) {
            log.error("系统未支持该数据源类型", e);
        }

        throw new Exception("系统未支持该数据源类型");
    }
}
