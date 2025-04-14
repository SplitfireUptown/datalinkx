package com.datalinkx.compute.transform;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.ConnectIdUtils;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/11/17 17:56
 */
@Slf4j
public final class ITransformFactory {

    private ITransformFactory() {

    }

    private static final String PACKAGE_PREFIX = "com.datalinkx.compute.transform.";


    public static ITransformDriver getComputeDriver(String type) throws Exception {
        String driverClassName = getDriverClass(type);
        Class<?> driverClass = Class.forName(driverClassName);
        Constructor<?> constructor = driverClass.getDeclaredConstructor();
        return (ITransformDriver) constructor.newInstance();
    }

    private static String getDriverClass(String type) {
        return PACKAGE_PREFIX + type.toUpperCase() + "TransformDriver";
    }
}
