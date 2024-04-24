package com.datalinkx.driver.dsdriver;


import java.util.List;

import com.datalinkx.driver.dsdriver.base.model.DbTree;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.model.DbTableField;


public interface IDsReader extends IDsDriver {
    String retrieveMax(FlinkActionMeta param, String field) throws Exception;
    Object getReaderInfo(FlinkActionMeta param) throws Exception;
    List<DbTree> tree(Boolean fetchTable) throws Exception;
    List<DbTree.DbTreeTable> treeTable(String catalog, String schema) throws Exception;
    List<DbTableField> getFields(String catalog, String schema, String tableName) throws Exception;
    default Boolean judgeIncrementalField(String catalog, String schema, String tableName, String field) throws Exception {
        return false;   
    }
    void afterRead(FlinkActionMeta param);
}
