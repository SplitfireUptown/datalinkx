package org.noear.solon.core.handle;

import org.noear.solon.Solon;
import org.noear.solon.core.FactoryManager;

/**
 * 覆盖源码，修改部分逻辑绕开校验
 */
public class ContextHolder {
    private static final ThreadLocal<Context> threadLocal = FactoryManager.getGlobal().newThreadLocal(ContextHolder.class, false);

    public ContextHolder() {
    }

    public static void currentSet(Context context) {
        threadLocal.set(context);
    }

    public static void currentRemove() {
        threadLocal.remove();
    }

    public static Context current() {
        Context tmp = (Context)threadLocal.get();
        // FIXME web框架还是使用spring，这里solon会有空指针bug，重写掉
        if (tmp == null && (Solon.cfg() == null || Solon.cfg().testing())) {
            tmp = new ContextEmpty();
            threadLocal.set(tmp);
        }

        return (Context) tmp;
    }
}
