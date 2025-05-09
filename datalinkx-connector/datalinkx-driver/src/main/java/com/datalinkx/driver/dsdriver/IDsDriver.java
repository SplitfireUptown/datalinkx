package com.datalinkx.driver.dsdriver;


public interface IDsDriver {
    Object connect(boolean check) throws Exception;
    default void disConnect(AutoCloseable conn) {
        if (null != conn) {
            try {
                conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    default String wrapColumnName(String columnName) {
        return columnQuota() + columnName + columnQuota();
    }
    default String wrapValue(String fieldType, String value) {
        return valueQuota() + value + valueQuota();
    }
    default String valueQuota() {
        return "";
    }
    default String columnQuota() {
        return "";
    }
}
