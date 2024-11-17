package com.datalinkx.compute.transform;

import com.datalinkx.common.utils.ConnectIdUtils;

import java.lang.reflect.Constructor;

/**
 * @author: uptown
 * @date: 2024/11/17 17:56
 */
public final class ITransformFactory {

    private ITransformFactory() {

    }

    private static final String PACKAGE_PREFIX = "com.datalinkx.compute.transform.";

    private static String getDriverClass(String type) {
        return PACKAGE_PREFIX + type.toUpperCase() + "TransformDriver";
    }

    public static ITransformDriver getComputeDriver(String type) throws Exception {
        String driverClassName = getDriverClass(type);
        Class<?> driverClass = Class.forName(driverClassName);
        Constructor<?> constructor = driverClass.getDeclaredConstructor();
        return (ITransformDriver) constructor.newInstance();
    }
}
