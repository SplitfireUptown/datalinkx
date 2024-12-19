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
        return null;
    }

    @Override
    public List<DbTableField> getFields(String catalog, String schema, String tableName) throws Exception {
        return null;
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
