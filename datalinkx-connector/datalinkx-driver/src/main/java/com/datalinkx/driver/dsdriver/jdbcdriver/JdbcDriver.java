package com.datalinkx.driver.dsdriver.jdbcdriver;


import java.lang.reflect.ParameterizedType;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.common.utils.TelnetUtil;
import com.datalinkx.compute.connector.jdbc.JdbcSink;
import com.datalinkx.compute.connector.jdbc.JdbcSource;
import com.datalinkx.compute.connector.jdbc.TransformNode;
import com.datalinkx.driver.dsdriver.IDsDriver;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.column.MetaColumn;
import com.datalinkx.driver.dsdriver.base.column.ReaderConnection;
import com.datalinkx.driver.dsdriver.base.column.WriterConnection;
import com.datalinkx.driver.dsdriver.base.connect.ConnectPool;
import com.datalinkx.driver.dsdriver.base.model.DbTableField;
import com.datalinkx.driver.dsdriver.base.model.DbTree;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.model.SeatunnelActionMeta;
import com.datalinkx.driver.dsdriver.base.reader.ReaderInfo;
import com.datalinkx.driver.dsdriver.base.writer.WriterInfo;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.google.common.collect.Lists;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JdbcDriver<T extends JdbcSetupInfo, P extends JdbcReader, Q extends JdbcWriter> implements
        AbstractDriver<T, P, Q>, IDsDriver, IDsReader, IDsWriter {

    protected T jdbcSetupInfo;
    protected String connectId;
    protected String PLUGIN_NAME = "Jdbc";

    private static final Set<String> INCREMENTAL_TYPE_SET = new HashSet<>();
    static {
        INCREMENTAL_TYPE_SET.add("datetime");
        INCREMENTAL_TYPE_SET.add("date");
        INCREMENTAL_TYPE_SET.add("timestamp");
        INCREMENTAL_TYPE_SET.add("time");
        INCREMENTAL_TYPE_SET.add("int");
        INCREMENTAL_TYPE_SET.add("double");
        INCREMENTAL_TYPE_SET.add("long");
        INCREMENTAL_TYPE_SET.add("bigint");
        INCREMENTAL_TYPE_SET.add("bigint unsigned");
    }

    public JdbcDriver(String connectId) {
        Class clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        this.jdbcSetupInfo = (T) JsonUtils.toObject(ConnectIdUtils.decodeConnectId(connectId), clazz);
        jdbcSetupInfo.setPwd(rebuildPassword(jdbcSetupInfo.getPwd()));
        this.connectId = connectId;
    }

    protected String jdbcUrl() {
        return "";
    }


    protected String driverClass() {
        return "";
    }

    protected Properties connectProp() {
        Properties info = new Properties();

        if (jdbcSetupInfo.getUid() != null) {
            info.put("user", jdbcSetupInfo.getUid());
        }
        if (jdbcSetupInfo.getPwd() != null) {
            info.put("password", jdbcSetupInfo.getPwd());
        }
        return info;
    }

    @Override
    public Object connect(boolean check) throws Exception {
        Connection connection;

        String url = jdbcUrl();
        String errorMsg;
        try {
            Class.forName(driverClass()).getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            errorMsg = "dsdriver class not exist";
            log.error(errorMsg, e);
            throw new Exception(errorMsg);
        }

        TelnetUtil.telnet(this.jdbcSetupInfo.getServer(), this.jdbcSetupInfo.getPort());

        try {
            connection = DriverManager.getConnection(url, connectProp());
        } catch (SQLException e) {
            errorMsg = "connect failed";
            log.error(errorMsg, e);
            throw new Exception("数据库连接失败, 原因：" + e.getMessage());
        }

        if (check) {
            connection.close();
        }

        return connection;
    }

    @Override
    public String getConnectId() {
        return this.connectId;
    }


    public List<DbTree.DbTreeTable> treeTable(String catalog, String schema) throws Exception {
        Connection connection = ConnectPool.getConnection(this, Connection.class);
        try {
            return generateTree(catalog, schema, connection);
        } finally {
            ConnectPool.releaseConnection(connectId, connection);
        }
    }

    @Override
    public List<DbTableField> getFields(String catalog, String schema, String tableName) throws Exception {
        Connection connection = ConnectPool.getConnection(this, Connection.class);
        List<Map<String, Object>> maps = fetchColumn(catalog, schema, tableName, connection);
        return JsonUtils.toList(JsonUtils.toJson(maps), DbTableField.class);
    }

    @Override
    public Boolean judgeIncrementalField(String catalog, String schema, String tableName, String field) throws Exception {
        List<DbTableField> incrementalFields = this.getFields(catalog, schema, tableName)
                .stream().filter(tableField -> tableField.getName().equals(field))
                .collect(Collectors.toList());

        if (ObjectUtils.isEmpty(incrementalFields)) {
            throw new Exception("增量字段不存在");
        }

        return INCREMENTAL_TYPE_SET.contains(incrementalFields.get(0).getType().toLowerCase());
    }



    public List<DbTree.DbTreeTable> generateTree(String catalog, String schema, Connection connection) {
        List<String> tableList = fetchTable(catalog, schema, connection);
        return tableList.stream().map(tableName -> {
            Map<String, Object> tableInfo = fetchTableInfo(catalog, schema, tableName, connection);
            DbTree.DbTreeTable dbTreeTable = new DbTree.DbTreeTable();
            dbTreeTable.setName(tableInfo.get("name") == null ? null : tableInfo.get("name").toString());
            dbTreeTable.setRemark(tableInfo.get("remark") == null ? null : tableInfo.get("remark").toString());
            dbTreeTable.setType(tableInfo.get("type") == null ? null : tableInfo.get("type").toString());
            dbTreeTable.setRef(refEncode(Lists.newArrayList(catalog, schema, tableName)));
            dbTreeTable.setLevel("table");
            return dbTreeTable;
        }).collect(Collectors.toList());
    }

    public String refEncode(List<String> refs) {
        return Base64.encode(JsonUtils.toJson(refs).getBytes());
    }

    @Override
    public String retrieveMax(FlinkActionMeta unit, String field) throws Exception {
        String fieldName = unit.getReader().getSync().getSyncCondition().getField();
        String catalog = unit.getReader().getCatalog();
        String schema = unit.getReader().getSchema();
        String tableName = unit.getReader().getTableName();

        String maxSql = String.format("select max(%s) from %s ", wrapColumnName(fieldName), wrapTableName(catalog, schema, tableName));

        log.info(String.format("retrieveMax, sql: %s", maxSql));
        Connection connection = ConnectPool.getConnection(this, Connection.class);
        try (Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            ResultSet resultSet = stmt.executeQuery(maxSql);
            if (resultSet.next()) {
                return resultSet.getString(1);
            }
        } catch (SQLException e) {
            log.error("fetch max error", e);
            throw new Exception("fetch max error");
        } finally {
            ConnectPool.releaseConnection(this.connectId, connection);
        }
        return null;
    }

    @Override
    public void truncateData(FlinkActionMeta param) throws Exception {
        String catalog = param.getWriter().getCatalog();
        String schema = param.getWriter().getSchema();
        String tableName = param.getWriter().getTableName();
        String fullTableName = wrapTableName(catalog, schema, tableName);

        Connection connection = ConnectPool.getConnection(this, Connection.class);
        try (Statement stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY)) {
            stmt.execute(String.format("truncate table %s", fullTableName));
        } catch (SQLException e) {
            log.error(String.format("truncate table (%s) failed", fullTableName), e);
        } finally {
            ConnectPool.releaseConnection(this.connectId, connection);
        }
    }


    public List<String> fetchTable(String catalog, String schema, Connection connection) {
        List<String> tables = new ArrayList<>();
        try {
            ResultSet resultSet = connection.getMetaData()
                    .getTables(catalog, schema, null, new String[]{"TABLE", "VIEW"});
            while (resultSet.next()) {
                if (!StringUtils.equals(resultSet.getString("TABLE_TYPE"), "SYSTEM TABLE")) {
                    tables.add(resultSet.getString("TABLE_NAME"));
                }
            }
        } catch (SQLException e) {
            log.error("fetch tables error", e);
        }

        return tables;
    }

    public Map<String, Object> fetchTableInfo(String catalog, String schema, String table, Connection connection) {
        Map<String, Object> result = new HashMap<>();

        String tableEscaped = table.replace(".", "%");

        ResultSet rs;
        try {
            rs = connection.getMetaData().getTables(catalog, schema, tableEscaped, null);
            while (rs.next()) {
                if (StringUtils.equals(rs.getString("TABLE_NAME"), table)) {
                    result.put("name", rs.getString("TABLE_NAME"));
                    result.put("remark", rs.getString("REMARKS"));
                    result.put("type", rs.getString("TABLE_TYPE"));
                }
            }
        } catch (SQLException e) {
            log.error("fetch table info error", e);
            throw new RuntimeException(e);
        }

        return result;
    }

    public List<Map<String, Object>> fetchColumn(String catalog, String schema, String table, Connection connection) {
        List<Map<String, Object>> result = new ArrayList<>();

        String tableEscaped = table.replace(".", "%");

        try {
            ResultSet rs = connection.getMetaData().getTables(catalog, schema, tableEscaped, null);
            boolean isExists = false;
            while (rs.next()) {
                if (StringUtils.equals(rs.getString("TABLE_NAME"), table)) {
                    isExists = true;
                    break;
                }
            }

            if (!isExists) {
                return new ArrayList<>();
            }

            ResultSet columns = connection.getMetaData().getColumns(catalog, schema, tableEscaped, null);
            while (columns.next()) {
                if (StringUtils.equals(table, columns.getString("TABLE_NAME"))) {
                    HashMap<String, Object> column = new HashMap<>();
                    column.put("name", columns.getString("COLUMN_NAME"));
                    column.put("type", columns.getString("TYPE_NAME"));
                    result.add(column);
                }
            }
        } catch (SQLException e) {
            log.error("fetch column error", e);
            throw new RuntimeException(e);
        }

        return result;
    }


    @Override
    public Object getReaderInfo(FlinkActionMeta unit) throws Exception {

        // 若是增量，查询最大值，作为下次的起始值
        String whereSql = unit.getDsReader().genWhere(unit);

        ReaderInfo<P> readerInfo = new ReaderInfo<>();
        String schema = unit.getReader().getSchema();
        String type = unit.getReader().getType();
        readerInfo.setName(type.toLowerCase() + "reader");

        readerInfo.setParameter((P) JdbcReader.builder()
                .username(jdbcSetupInfo.getUid())
                .password(jdbcSetupInfo.getPwd())
                .fetchSize(unit.getReader().getSync().getFetchSize())
                .queryTimeOut(unit.getReader().getSync().getQueryTimeOut())
                .connection(Collections.singletonList(ReaderConnection.builder()
                        .jdbcUrl(Collections.singletonList(jdbcUrl()))
                        .table(Collections.singletonList(unit.getReader().getTableName()))
                        .schema(schema)
                        .build()))
                .column(
                        ObjectUtils.isEmpty(unit.getReader().getColumns()) ?
                                Collections.singletonList(
                                        MetaColumn.builder()
                                                .name("*")
                                                .build()
                                )
                                :
                                unit.getReader().getColumns().stream().map(col ->
                                MetaColumn.builder()
                                        .name(col.getName())
                                        .build())
                        .collect(Collectors.toList()))
                .where(whereSql)
                .build());

        return readerInfo;
    }

    public Object getWriterInfo(FlinkActionMeta unit) {
        WriterInfo<Q> jdbcWriterInfo = new WriterInfo<>();

        List<String> preSql = new ArrayList<>();

        String schema = unit.getWriter().getSchema();
        String type = unit.getWriter().getType();
        jdbcWriterInfo.setName(type.toLowerCase() + "writer");
        jdbcWriterInfo.setParameter((Q) JdbcWriter.builder()
                .username(jdbcSetupInfo.getUid())
                .password(jdbcSetupInfo.getPwd())
                .connection(Collections.singletonList(WriterConnection.builder()
                        .schema(schema)
                        .jdbcUrl(jdbcUrl())
                        .table(Collections.singletonList(unit.getWriter().getTableName()))
                        .build()))
                .column(unit.getWriter().getColumns().stream().map(DataTransJobDetail.Column::getName).collect(Collectors.toList()))
                .insertSqlMode("copy")
                .writeMode("insert")
                .batchSize(unit.getWriter().getBatchSize())
                .preSql(preSql)
                .build());

        return jdbcWriterInfo;
    }

    public String columnQuota() {
        return "`";
    }

    public String valueQuota() {
        return "'";
    }

    public String wrapTableName(String catalog, String schema, String tableName) {
        List<String> fullName = new ArrayList<>();
        if (StringUtils.isNotEmpty(catalog)) {
            fullName.add(String.format("%s%s%s", columnQuota(), catalog, columnQuota()));
        }

        if (StringUtils.isNotEmpty(schema)) {
            fullName.add(String.format("%s%s%s", columnQuota(), schema, columnQuota()));
        }

        if (StringUtils.isNotEmpty(tableName)) {
            fullName.add(String.format("%s%s%s", columnQuota(), tableName, columnQuota()));
        }
        return String.join(".", fullName);
    }

    @Override
    public TransformNode getSourceInfo(FlinkActionMeta unit) {

        return JdbcSource.builder()
                .url(this.jdbcUrl())
                .driver(this.driverClass())
                .user(this.jdbcSetupInfo.getUid())
                .password(this.jdbcSetupInfo.getPwd())
                .query(this.transferSourceSQL(unit))
                .pluginName(PLUGIN_NAME)
                .resultTableName(MetaConstants.CommonConstant.SOURCE_TABLE)
                .build();
    }

    @Override
    public String transferSourceSQL(FlinkActionMeta unit) {
        DataTransJobDetail.Reader reader = unit.getReader();

        String sourceSQL = String.format("select %s from %s.%s", reader.getQueryFields(), reader.getSchema(), reader.getTableName());
        try {

            String increaseSQL = this.genWhere(unit);
            if (!ObjectUtils.isEmpty(increaseSQL)) {
                sourceSQL += String.format(" where %s", increaseSQL);
            }
        } catch (Exception e) {

            log.error("gen increase sql error: " + e.getMessage(), e);
            throw new DatalinkXJobException("生成增量条件SQL失败");
        }

        return sourceSQL;
    }

    @Override
    public TransformNode getSinkInfo(SeatunnelActionMeta param) {


        return JdbcSink.builder()
                .url(this.jdbcUrl())
                .driver(this.driverClass())
                .user(this.jdbcSetupInfo.getUid())
                .password(this.jdbcSetupInfo.getPwd())
                .pluginName(PLUGIN_NAME)
                .query(this.transferSinkSQL(param))
                .build();
    }

    @Override
    public String transferSinkSQL(SeatunnelActionMeta param) {
        DataTransJobDetail.Writer writer = param.getWriter();
        StringBuilder abstractQuery = new StringBuilder();
        for (int i = 0; i < writer.getInsertFields().split(",").length; i++) {
            if (i == 0) {

                abstractQuery.append("?");
            } else {

                abstractQuery.append(",?");
            }
        }


        return String.format("insert into %s.%s(%s) values(%s)", writer.getSchema(), writer.getTableName(), writer.getInsertFields(), abstractQuery);
    }
}
