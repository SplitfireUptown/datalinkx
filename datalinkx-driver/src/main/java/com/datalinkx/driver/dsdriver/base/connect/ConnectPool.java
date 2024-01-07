package com.datalinkx.driver.dsdriver.base.connect;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.datalinkx.driver.dsdriver.IDsDriver;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public final class ConnectPool {
    private static Map<String, BlockingDeque<Object>> connMap = new ConcurrentHashMap<>();
    private static Map<String, Long> elapseTime = new ConcurrentHashMap<>();

    private static Long expiredTimeValue = 1000 * 60 * 60L;
    private static Long deletePeriod = 1000 * 60 * 60L;
    private static boolean usePool = false;

    private ConnectPool() { }

    static {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(ConnectPool::checkAndFreeConnection, 0, deletePeriod, TimeUnit.MILLISECONDS);
    }

    public static <T extends IDsDriver, C> C getConnection(T t, Class<C> clazz) throws Exception {
        String connectId = t.getConnectId();
        if (!usePool) {
            return (C) t.connect(false);
        }
        synchronized (t.getConnectId().intern()) {
            if (connMap.containsKey(connectId) && !connMap.get(connectId).isEmpty()) {
                C conn = (C) connMap.get(connectId).poll();
                if (null != conn) {
                    try {
                        t.checkConnectAlive(conn);
                        return conn;
                    } catch (Exception e) {
                        log.error("check alive failed", e);
                        try {
                            ((AutoCloseable) conn).close();
                        } catch (Exception ee) {
                            log.error("close failed", ee);
                        }
                        return (C) t.connect(false);
                    }
                }
            }

            return (C) t.connect(false);
        }
    }

    public static <T extends IDsDriver> void releaseConnection(String connectId, Object connection) {
        if (!usePool) {
            closeConn((AutoCloseable) connection);
        }

        synchronized (connectId.intern()) {
            if (!connMap.containsKey(connectId)) {
                connMap.put(connectId, new LinkedBlockingDeque<>());
            }
            connMap.get(connectId).offer(connection);
            elapseTime.put(connectId, new Date().getTime());
        }
    }

    private static void checkAndFreeConnection() {
        Long timestamp = new Date().getTime();
        List<String> deleteList = new ArrayList<>();
        for (Map.Entry<String, Long> entry : elapseTime.entrySet()) {
            if (timestamp - entry.getValue() > expiredTimeValue) {
                deleteList.add(entry.getKey());
            }
        }

        for (String del : deleteList) {
            while (!connMap.get(del).isEmpty()) {
                if (timestamp - elapseTime.get(del) > expiredTimeValue) {
                    AutoCloseable conn = (AutoCloseable) connMap.get(del).pollLast();
                    if (null != conn) {
                        closeConn(conn);
                        log.info(String.format("close connection: %s", del));
                    } else {
                        log.info(String.format("reused connection: %s", del));
                    }
                } else {
                    log.info(String.format("new connection when deleting: %s", del));
                    break;
                }
            }

            if (connMap.get(del).isEmpty()) {
                elapseTime.remove(del);
            }
        }
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
