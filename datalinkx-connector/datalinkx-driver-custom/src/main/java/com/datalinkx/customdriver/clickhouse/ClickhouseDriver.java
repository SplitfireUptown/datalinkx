package com.datalinkx.customdriver.clickhouse;

import com.datalinkx.common.result.DatalinkXJobDetail;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.meta.DbTableField;
import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcReader;
import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcWriter;
import com.datalinkx.driver.dsdriver.setupinfo.CustomSetupInfo;

import java.util.Collections;
import java.util.List;

public class ClickhouseDriver implements AbstractDriver<CustomSetupInfo, JdbcReader, JdbcWriter>, IDsReader, IDsWriter {

    @Override
    public String retrieveMax(DatalinkXJobDetail.Reader reader, String field) throws Exception {
        return "";
    }

    @Override
    public Object getReaderInfo(DatalinkXJobDetail.Reader reader) throws Exception {
        return null;
    }

    @Override
    public List<String> treeTable(String catalog, String schema) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public List<DbTableField> getFields(String catalog, String schema, String tableName) throws Exception {
        return Collections.emptyList();
    }

    @Override
    public Object connect(boolean check) throws Exception {
        return null;
    }

    @Override
    public void truncateData(DatalinkXJobDetail.Writer writer) throws Exception {

    }

    @Override
    public Object getWriterInfo(DatalinkXJobDetail.Writer writer) throws Exception {
        return null;
    }
}
