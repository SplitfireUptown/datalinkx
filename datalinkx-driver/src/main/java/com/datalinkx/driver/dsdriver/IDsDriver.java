package com.datalinkx.driver.dsdriver;


import com.datalinkx.driver.dsdriver.base.model.TableStruct;

public interface IDsDriver {
    Object connect(boolean check) throws Exception;
    String getConnectId();
    TableStruct describeTbAndField(String catalog, String schema, String tableId, String tableName, boolean includeField) throws Exception;
    void checkConnectAlive(Object conn) throws Exception;
}
