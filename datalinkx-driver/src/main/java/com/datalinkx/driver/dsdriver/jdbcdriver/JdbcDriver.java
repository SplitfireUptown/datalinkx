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

import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.common.utils.TelnetUtil;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.SqlGenerator;
import com.datalinkx.driver.dsdriver.base.column.MetaColumn;
import com.datalinkx.driver.dsdriver.base.column.ReaderConnection;
import com.datalinkx.driver.dsdriver.base.column.WriterConnection;
import com.datalinkx.driver.dsdriver.base.connect.ConnectPool;
import com.datalinkx.driver.dsdriver.base.model.DbTree;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionParam;
import com.datalinkx.driver.dsdriver.base.model.TableField;
import com.datalinkx.driver.dsdriver.base.reader.ReaderInfo;
import com.datalinkx.driver.dsdriver.base.writer.WriterInfo;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.google.common.collect.Lists;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class JdbcDriver<T extends JdbcSetupInfo, P extends JdbcReader, Q extends JdbcWriter> extends SqlGenerator implements AbstractDriver<T, P, Q>, IDsReader, IDsWriter {
        private static int defaultFetchSize = 10000;
    private static int defaultQueryTimeOut = 100000;
    private static String allowNull = "1";

    protected T jdbcSetupInfo;
    protected String connectId;


    private static final Set<String> INCREMENTAL_TYPE_SET = new HashSet<>();
    static {
        INCREMENTAL_TYPE_SET.add("datetime");
        INCREMENTAL_TYPE_SET.add("date");
        INCREMENTAL_TYPE_SET.add("timestamp");
        INCREMENTAL_TYPE_SET.add("time");
        INCREMENTAL_TYPE_SET.add("int");
        INCREMENTAL_TYPE_SET.add("double");
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
        Connection connection = null;

        String url = jdbcUrl();
        String errorMsg = "";
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

    public List<DbTree> tree(Boolean fetchTable) throws Exception {
        Connection connection = ConnectPool.getConnection(this, Connection.class);
        try {
            List<String> catalogs = fetchCatalog(connection);
            List<DbTree> result = new ArrayList<>();

            if (!ObjectUtils.isEmpty(catalogs)) {
                List<DbTree> catalogList = catalogs.stream().map(catalog -> {
                    DbTree catalogItem = new DbTree();
                    catalogItem.setName(catalog);
                    catalogItem.setLevel("catalog");
                    catalogItem.setRef(refEncode(Lists.newArrayList(catalog, null, null)));
                    List<? extends DbTree> dbTrees = generateTree(catalog, fetchTable, connection);
                    if (dbTrees.size() > 0) {
                        if (dbTrees.get(0) instanceof DbTree.DbTreeTable) {
                            catalogItem.setTable(dbTrees.stream()
                                    .map(dbTree -> (DbTree.DbTreeTable) dbTree).collect(Collectors.toList()));
                        } else {
                            catalogItem.setFolder(dbTrees.stream()
                                    .map(dbTree -> (DbTree) dbTree).collect(Collectors.toList()));
                        }
                    }
                    return catalogItem;
                }).collect(Collectors.toList());

                List<DbTree> schemaList = new ArrayList<>();
                catalogList.forEach(catalog -> schemaList.addAll(catalog.getFolder()));
                result.addAll(schemaList);
                return result;
            } else {
                List<? extends DbTree> tree = generateTree(null, fetchTable, connection);
                result.addAll(tree);
            }

            return result;
        } finally {
            ConnectPool.releaseConnection(this.connectId, connection);
        }
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
    public List<TableField> getFields(String catalog, String schema, String tableName) throws Exception {
        Connection connection = ConnectPool.getConnection(this, Connection.class);
        List<Map<String, Object>> maps = fetchColumn(catalog, schema, tableName, connection);
        return JsonUtils.toList(JsonUtils.toJson(maps), TableField.class);
    }

    @Override
    public Boolean judgeIncrementalField(String catalog, String schema, String tableName, String field) throws Exception {
        List<TableField> incrementalFields = this.getFields(catalog, schema, tableName)
                .stream().filter(tableField -> tableField.getName().equals(field))
                .collect(Collectors.toList());

        if (ObjectUtils.isEmpty(incrementalFields)) {
            throw new Exception("增量字段不存在");
        }

        return INCREMENTAL_TYPE_SET.contains(incrementalFields.get(0).getType().toLowerCase());
    }


    @Override
    public void afterRead(FlinkActionParam param) {
    }

    public List<? extends DbTree> generateTree(String catalog, boolean fetchTable, Connection connection) {
        List<String> schemas = fetchSchema(catalog, connection);
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        if (schemas == null) {
            if (fetchTable) {
                return generateTree(catalog, null, connection);
            }
            return new ArrayList<>();
        }

        return schemas.stream().map(schema -> {
            DbTree schemaItem = new DbTree();
            schemaItem.setName(schema);
            schemaItem.setLevel("schema");
            schemaItem.setRef(refEncode(Lists.newArrayList(catalog, schema, null)));
            schemaItem.setTable(fetchTable ? generateTree(catalog, schema, connection) : new ArrayList<>());
            return schemaItem;
        }).collect(Collectors.toList());
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
    public void checkConnectAlive(Object conn) throws Exception {
        Connection connection = (Connection) conn;
        connection.getCatalog();
    }



    @Override
    public String retrieveMax(FlinkActionParam unit, String field) throws Exception {
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
    public void truncateData(FlinkActionParam param) throws Exception {
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


    public List<String> fetchCatalog(Connection connection) {
        ResultSet resultSet = null;
        List<String> catalogs = new ArrayList<>();
        try {
            resultSet = connection.getMetaData().getCatalogs();
            while (resultSet.next()) {
                catalogs.add(resultSet.getString("TABLE_CAT"));
            }
        } catch (SQLException e) {
            log.error("fetch catalog error", e);
        }
        return catalogs;
    }

    public List<String> fetchSchema(String catalog, Connection connection) {
        ResultSet resultSet = null;
        List<String> schemas = new ArrayList<>();
        try {
            resultSet = connection.getMetaData().getSchemas(catalog, null);
            while (resultSet.next()) {
                schemas.add(resultSet.getString("TABLE_SCHEM"));
            }
        } catch (SQLException e) {
            log.error("fetch schema error", e);
        }
        return schemas;
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

        ResultSet rs = null;
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
        Set<String> primaryKeysHash;
        Set<String> queryIndexHash = new HashSet<String>();

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

            primaryKeysHash = this.fetchPrimaryKeys(catalog, schema, table, connection);

            try {
                ResultSet queryIndex = connection.getMetaData().getIndexInfo(catalog, schema, table, false, getApproximate());
                while (queryIndex.next()) {
                    queryIndexHash.add(queryIndex.getString("COLUMN_NAME"));
                }
            } catch (SQLException e) {
                log.warn(String.format("Get index for %s.%s.%s failed", catalog, schema, table), e);
            }

            ResultSet columns = connection.getMetaData().getColumns(catalog, schema, tableEscaped, null);
            while (columns.next()) {
                // MySQL中表名包含"."时会导致去不到列. See also: https://github.com/mysql/mysql-connector-j
                if (StringUtils.equals(table, columns.getString("TABLE_NAME"))) {
                    HashMap<String, Object> column = new HashMap<>();
                    column.put("name", columns.getString("COLUMN_NAME"));
                    column.put("type", columns.getString("TYPE_NAME"));
                    column.put("raw_type", columns.getString("TYPE_NAME"));
                    column.put("remark", columns.getString("REMARKS"));
                    column.put("uniq_index", primaryKeysHash.contains(columns.getString("COLUMN_NAME")));
                    column.put("query_index", queryIndexHash.contains(columns.getString("COLUMN_NAME")));
                    column.put("position", columns.getString("ORDINAL_POSITION"));
                    column.put("allow_null", allowNull.equals(columns.getString("NULLABLE")));

                    result.add(column);
                }
            }
        } catch (SQLException e) {
            log.error("fetch column error", e);
            throw new RuntimeException(e);
        }

        return result;
    }

    protected Set<String> fetchPrimaryKeys(String catalog, String schema, String table, Connection connection) {
        Set<String> primaryKeysHash = new HashSet<>();
        try {
            ResultSet primaryKeys = connection.getMetaData().getPrimaryKeys(catalog, schema, table);
            while (primaryKeys.next()) {
                primaryKeysHash.add(primaryKeys.getString("COLUMN_NAME"));
            }
        }  catch (SQLException e) {
            log.error("fetch column error", e);
            throw new RuntimeException(e);
        }
        return primaryKeysHash;
    }

    protected boolean getApproximate() {
        return false;
    }

    @Override
    public Object getReaderInfo(FlinkActionParam unit) throws Exception {

        // 若是增量，查询最大值，作为下次的起始值
        String whereSql = unit.getDsReader().genWhere(unit).generate();

        ReaderInfo<P> readerInfo = new ReaderInfo<>();
        String schema = unit.getReader().getSchema();
        String type = unit.getReader().getType();
        readerInfo.setName(type.toLowerCase() + "reader");

        readerInfo.setParameter((P) JdbcReader.builder()
                .username(jdbcSetupInfo.getUid())
                .password(jdbcSetupInfo.getPwd())
                .fetchSize(defaultFetchSize)
                .queryTimeOut(defaultQueryTimeOut)
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

    public Object getWriterInfo(FlinkActionParam unit) {
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
                .preSql(preSql)
                .build());

        return jdbcWriterInfo;
    }

    @Override
    public void afterWrite(FlinkActionParam param) {

    }

    protected String columnQuota() {
        return "`";
    }

    protected String valueQuota() {
        return "'";
    }


    protected String wrapTableName(String catalog, String schema, String tableName) {
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
}
