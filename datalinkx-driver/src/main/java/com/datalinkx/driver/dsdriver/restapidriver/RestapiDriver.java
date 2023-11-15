package com.datalinkx.driver.dsdriver.restapidriver;


import com.datalinkx.common.sql.SqlOperator;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.driver.dsdriver.base.model.*;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.model.*;
import lombok.extern.slf4j.Slf4j;


import java.util.List;


@Slf4j
public class RestapiDriver implements AbstractDriver<RestapiSetupInfo, RestapiReader, RestapiWriter>, IDsReader, IDsWriter {

    private String connectId;

    private RestapiSetupInfo restapiSetupInfo;

    public RestapiDriver(String connectId) {
        this.restapiSetupInfo = JsonUtils.toObject(connectId, RestapiSetupInfo.class);
        this.connectId = connectId;
    }

    @Override
    public Object connect(boolean check) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getConnectId() {
        return connectId;
    }

    @Override
    public TableStruct describeTbAndField(String catalog, String schema, String tableId, String tableName, boolean includeField) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public void checkConnectAlive(Object conn) throws Exception {
        throw new UnsupportedOperationException();
    }


    @Override
    public String retrieveMax(FlinkActionParam param, String field) throws Exception {
        return null;
    }

    @Override
    public Object getReaderInfo(FlinkActionParam param) throws Exception {
        return null;
    }

    @Override
    public List<DbTree> tree(Boolean fetchTable) throws Exception {
        return null;
    }

    @Override
    public List<DbTree.DbTreeTable> treeTable(String catalog, String schema) throws Exception {
        return null;
    }

    @Override
    public List<TableField> getFields(String catalog, String schema, String tableName) throws Exception {
        return null;
    }


    @Override
    public void afterRead(FlinkActionParam param) {

    }



    @Override
    public SqlOperator genWhere(FlinkActionParam param) throws Exception {
        return null;
    }


    @Override
    public void truncateData(FlinkActionParam param) throws Exception {

    }


    @Override
    public Object getWriterInfo(FlinkActionParam param) {
        return null;
    }

    @Override
    public void afterWrite(FlinkActionParam param) {

    }
}
