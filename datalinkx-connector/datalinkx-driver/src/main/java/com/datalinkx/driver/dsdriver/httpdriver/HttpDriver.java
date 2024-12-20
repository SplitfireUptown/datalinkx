package com.datalinkx.driver.dsdriver.httpdriver;

import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.TelnetUtil;
import com.datalinkx.compute.connector.jdbc.TransformNode;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.model.DbTableField;
import com.datalinkx.driver.dsdriver.base.model.DbTree;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;
import com.datalinkx.driver.dsdriver.esdriver.EsSetupInfo;
import com.datalinkx.driver.dsdriver.esdriver.OpenEsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author: uptown
 * @date: 2024/12/17 22:38
 */
public class HttpDriver implements AbstractDriver<HttpSetupInfo, HttpReader, AbstractWriter>, IDsReader {

    private final String connectId;
    private final HttpSetupInfo httpSetupInfo;

    public HttpDriver(String connectId) {
        this.connectId = connectId;
        this.httpSetupInfo = JsonUtils.toObject(ConnectIdUtils.decodeConnectId(connectId) , HttpSetupInfo.class);
    }

    @Override
    public Object connect(boolean check) throws Exception {
        TelnetUtil.telnet(this.httpSetupInfo.getUrl(), this.httpSetupInfo.getPort());
        return this.httpSetupInfo;
    }

    @Override
    public String getConnectId() {
        return null;
    }


    @Override
    public String retrieveMax(FlinkActionMeta param, String field) throws Exception {
        return null;
    }

    @Override
    public Object getReaderInfo(FlinkActionMeta param) throws Exception {
        return null;
    }

    @Override
    public List<DbTree.DbTreeTable> treeTable(String catalog, String schema) throws Exception {
        DbTree.DbTreeTable dbTreeTable = new DbTree.DbTreeTable();
        dbTreeTable.setName("json_path返回解析表");
        dbTreeTable.setLevel("table");
        return Collections.singletonList(dbTreeTable);
    }

    @Override
    public List<DbTableField> getFields(String catalog, String schema, String tableName) throws Exception {
        String revData = this.httpSetupInfo.getRevData();
        JsonNode responseJsonNode = JsonUtils.toJsonNode(revData);
        List<DbTableField> result = new ArrayList<>();

        // 如果json_path解析结果是数组，取数组的第一个元素里的所有key作为HTTP数据源下对应的表
        if (responseJsonNode.isArray()) {
            JsonNode firstElement = responseJsonNode.get(0);
            Iterator<String> fieldNames = firstElement.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                result.add(DbTableField.builder().name(key).build());
            }
        } else {
            Iterator<String> fieldNames = responseJsonNode.fieldNames();
            while (fieldNames.hasNext()) {
                String key = fieldNames.next();
                result.add(DbTableField.builder().name(key).build());
            }
        }
        return result;
    }

    @Override
    public TransformNode getSourceInfo(FlinkActionMeta unit) {
        return IDsReader.super.getSourceInfo(unit);
    }

    @Override
    public String transferSourceSQL(FlinkActionMeta unit) {
        return IDsReader.super.transferSourceSQL(unit);
    }
}
