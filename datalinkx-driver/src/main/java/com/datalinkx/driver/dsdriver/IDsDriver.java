package com.datalinkx.driver.dsdriver;



public interface IDsDriver {
    Object connect(boolean check) throws Exception;
    String getConnectId();
    void checkConnectAlive(Object conn) throws Exception;
}
