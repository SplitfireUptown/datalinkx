package com.datalinkx.driver.dsdriver;


import java.util.List;

import com.datalinkx.common.sql.SqlOperator;
import com.datalinkx.driver.dsdriver.base.model.DbTree;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionParam;
import com.datalinkx.driver.dsdriver.base.model.TableField;


public interface IDsReader extends IDsDriver {
    String retrieveMax(FlinkActionParam param, String field) throws Exception;
    Object getReaderInfo(FlinkActionParam param) throws Exception;
    SqlOperator genWhere(FlinkActionParam param) throws Exception;
    List<DbTree> tree(Boolean fetchTable) throws Exception;
    List<DbTree.DbTreeTable> treeTable(String catalog, String schema) throws Exception;
    List<TableField> getFields(String catalog, String schema, String tableName) throws Exception;
    default Boolean judgeIncrementalField(String catalog, String schema, String tableName, String field) throws Exception {
        return false;   
    }
    void afterRead(FlinkActionParam param);
}
