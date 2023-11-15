package com.datalinkx.driver.utils;


import com.datalinkx.driver.model.JobContext;

public final class JobUtils {
    private JobUtils() { }
    private static ThreadLocal<JobContext> contextThreadLocal = new ThreadLocal<>();

    public static JobContext cntx() {
        return contextThreadLocal.get();
    }

    public static void initContext() {
        contextThreadLocal.set(new JobContext());
    }

    public static void finiContext() {
        contextThreadLocal.remove();
    }

    public static void configContext(JobContext context) {
        if (null == context) {
            contextThreadLocal.remove();
        } else {
            contextThreadLocal.set(context);
        }
    }
}
