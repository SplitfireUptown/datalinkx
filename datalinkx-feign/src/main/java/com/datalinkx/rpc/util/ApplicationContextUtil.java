package com.datalinkx.rpc.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @author: uptown
 * @date: 2025/4/12 13:15
 */
@Component
public class ApplicationContextUtil implements ApplicationContextAware {
    private static ApplicationContext context;

    private ApplicationContextUtil() {
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ApplicationContextUtil.setContext(applicationContext);
    }

    public static <T> T getBean(Class<T> t) {
        return context.getBean(t);
    }

    public static Object getBean(String name) {
        return context.getBean(name);
    }

    public static void setContext(ApplicationContext context) {
        ApplicationContextUtil.context = context;
    }
}
