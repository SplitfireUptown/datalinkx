package com.datalinkx.driver.dsdriver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.driver.dsdriver.customdriver.CustomSetupInfo;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.core.Map;


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

        // 如果是自定义数据源，解析config中的类型进行反射加载对应插件
        if (MetaConstants.DsType.DS_CUSTOM.equalsIgnoreCase(dsType)) {
            CustomSetupInfo customSetupInfo = JsonUtils.toObject(ConnectIdUtils.decodeConnectId(connectId), CustomSetupInfo.class);
            Map object = JsonUtils.toObject(customSetupInfo.getConfig(), Map.class);
            // TODO: 加载对应自定义插件
        }

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
