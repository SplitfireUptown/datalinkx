package com.datalinkx.driver.dsdriver.base.connect;


import com.datalinkx.driver.dsdriver.IDsDriver;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class ConnectPool {

    private ConnectPool() { }


    public static <T extends IDsDriver, C> C getConnection(T t, Class<C> clazz) throws Exception {
        return (C) t.connect(false);
    }

    public static <T extends IDsDriver> void releaseConnection(String connectId, Object connection) {
        closeConn((AutoCloseable) connection);
    }

    private static void closeConn(AutoCloseable conn) {
        if (null != conn) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
