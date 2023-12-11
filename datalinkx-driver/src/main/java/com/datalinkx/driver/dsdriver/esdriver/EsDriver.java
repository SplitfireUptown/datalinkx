package com.datalinkx.driver.dsdriver.esdriver;

import com.datalinkx.common.sql.SqlOperator;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.base.AbstractDriver;
import com.datalinkx.driver.dsdriver.base.column.MetaColumn;
import com.datalinkx.driver.dsdriver.base.model.DbTree;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionParam;
import com.datalinkx.driver.dsdriver.base.model.TableField;
import com.datalinkx.driver.dsdriver.base.model.TableStruct;
import com.datalinkx.driver.dsdriver.base.reader.ReaderInfo;
import com.datalinkx.common.utils.ConnectIdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.common.utils.RefUtils;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class EsDriver implements AbstractDriver<EsSetupInfo, EsReader, EsWriter>, IDsReader {
    private static final long ES_TIMEOUT = 3000L;
    private static final long DEFAULT_FETCH_SIZE = 10000L;

    private String connectId;
    private EsSetupInfo esSetupInfo;
    private EsService esService;

    public EsDriver(String connectId) {
        this.connectId = connectId;
        this.esSetupInfo = JsonUtils.toObject(ConnectIdUtils.decodeConnectId(connectId) , EsSetupInfo.class);
        this.esService = new OpenEsService(esSetupInfo, this);
    }

    @Override
    public Object connect(boolean check) throws Exception {
        return esService.getClient();
    }

    @Override
    public String getConnectId() {
        return this.connectId;
    }

    @Override
    public SqlOperator genWhere(FlinkActionParam param) throws Exception {
        return null;
    }

    @Override
    public void checkConnectAlive(Object conn) throws Exception {
        esService.checkConnectAlive(conn);
    }


    private Map<String, Object> getBoolQuery(FlinkActionParam param) throws Exception {

        List<Map<String, Object>> mustList = new ArrayList<>();
        if (param.getReader().getSync().getSyncCondition() != null) {
            String field = param.getReader().getSync().getSyncCondition().getField();
            String fieldType = param.getReader().getSync().getSyncCondition().getFieldType();


            if ("increment".equalsIgnoreCase(param.getReader().getSync().getType())) {
                String startValue = param.getReader().getSync().getSyncCondition().getStart().getValue();
                if (param.getReader().getSync().getSyncCondition().getStart().getEnable() == 1 && startValue != null) {
                    String op = param.getReader().getSync().getSyncCondition().getStart().getOperator();
                    mustList.add(esService.buildRange(field, startValue, op, "must", fieldType));
//                    boolQueryBuilder.must(QueryBuilders.rangeQuery(field).gt(value));
                }

                String endValue = param.getReader().getSync().getSyncCondition().getEnd().getValue();
                if (param.getReader().getSync().getSyncCondition().getEnd().getEnable() == 1) {
//                    boolQueryBuilder.must(QueryBuilders.rangeQuery(field).lte(value));
                    String op = param.getReader().getSync().getSyncCondition().getEnd().getOperator();
                    mustList.add(esService.buildRange(field, endValue, op, "must", fieldType));
                }
            }
        }

        List<Map<String, Object>> shouldList = new ArrayList<>();

        Map<String, Object> queryMap = new HashMap<>();
        if (!mustList.isEmpty()) {
            queryMap.put("must", mustList);
        }

        if (!shouldList.isEmpty()) {
            queryMap.put("should", shouldList);
            queryMap.put("minimum_should_match", 1);
        }

        Map<String, Object> boolMap = new HashMap<>();
        boolMap.put("bool", queryMap);

        return boolMap;
    }

    @Override
    public String retrieveMax(FlinkActionParam param, String maxField) throws Exception {
        String fieldType = param.getReaderFieldType(maxField);
        if (!"number".equals(fieldType) && !"date".equals(fieldType)) {
            throw new Exception(String.format("字段%s类型为%s, 不支持增量同步", maxField, fieldType));
        }

        Map<String, Object> boolMap = getBoolQuery(param);
        Map<String, Object> queryMap = new HashMap<String, Object>() {
            {
                put("query", boolMap);
                put("sort", Lists.newArrayList(new HashMap<String, Object>() {
                    {
                        put(maxField, "desc");
                    }
                }));
                put("size", 1);
            }
        };
        return esService.retrieveMax(param.getReader().getTableName(), JsonUtils.toJson(queryMap), maxField);
    }


    @Override
    public Object getReaderInfo(FlinkActionParam param) throws Exception {
        Map<String, Object> boolMap = getBoolQuery(param);
        Map<String, Object> mustMap = (Map<String, Object>) boolMap.get("bool");
        if (param.getReader().getSync().getSyncCondition() != null
                && StringUtils.isEmpty(param.getReader().getMaxValue())) {
            String maxField = param.getReader().getSync().getSyncCondition().getField();
            String maxValue = retrieveMax(param, maxField);
            if (null != maxValue) {
                param.getReader().setMaxValue(maxValue);
                String fieldType = param.getReader().getSync().getSyncCondition().getFieldType();
                if (mustMap.get("must") != null) {
                    ((List<Map<String, Object>>) mustMap.get("must"))
                            .add(esService.buildRange(maxField, maxValue, "<=", "must", fieldType));
                } else {
                    List<Map<String, Object>> mustList = new ArrayList<>();
                    mustList.add(esService.buildRange(maxField, maxValue, "<=", "must", fieldType));
                    mustMap.put("must", mustList);
                }
            }
        }

        List<String> types = new ArrayList<>();
        types.addAll(esService.getIndexType(param.getReader().getTableName()));

        ReaderInfo<EsReader> readerInfo = new ReaderInfo<>();
        readerInfo.setName("esreader");


        readerInfo.setParameter(EsReader.builder()
                .address(esSetupInfo.getAddress())
                .username(esSetupInfo.getUid())
                .password(esSetupInfo.getPwd())
                .kerberosKrb5Path(esSetupInfo.getKerberosKrb5Path())
                .kerberosJaasPath(esSetupInfo.getKerberosJaasPath())
                .batchSize(param.getReader().getSync().getFetchSize() == null ? DEFAULT_FETCH_SIZE
                        : param.getReader().getSync().getFetchSize().longValue())
                .index(param.getReader().getTableName())
                .type(types.toArray(new String[0]))
                .query(JsonUtils.toJsonNode(JsonUtils.toJson(boolMap)))
                .timeout(ES_TIMEOUT).column(param.getReader().getColumns().stream()
                        .map(col -> MetaColumn.builder()
                                .name(col.getName())
                                .format(col.getFormat())
                                .value("DX_AUDIT".equals(col.getName()) ? "now()" : null)
                                .build()).collect(Collectors.toList()))
                .build());

        return readerInfo;
    }

    @Override
    public List<DbTree> tree(Boolean fetchTable) throws Exception {
        List<String> indexes = esService.getIndexes();
        DbTree dbTree = new DbTree();
        dbTree.setRef(RefUtils.encode(Lists.newArrayList("", null, null)));
        dbTree.setName("");
        dbTree.setLevel("catalog");
        dbTree.setTable(new ArrayList<>());
        if (fetchTable) {
            indexes.forEach(idx -> {
                DbTree.DbTreeTable dbTreeTable = new DbTree.DbTreeTable();
                dbTreeTable.setName(idx);
                dbTreeTable.setRemark("");
                dbTreeTable.setType("table");
                dbTreeTable.setLevel("table");
                dbTreeTable.setRef(RefUtils.encode(Lists.newArrayList("", null, idx)));
                dbTree.getTable().add(dbTreeTable);
            });
        }

        return Lists.newArrayList(dbTree);
    }

    @Override
    public List<DbTree.DbTreeTable> treeTable(String catalog, String schema) throws Exception {
        List<DbTree.DbTreeTable> dbTreeTables = new ArrayList<>();
        if ("".equals(catalog)) {
            List<String> indexes = esService.getIndexes();
            indexes.forEach(idx -> {
                DbTree.DbTreeTable dbTreeTable = new DbTree.DbTreeTable();
                dbTreeTable.setName(idx);
                dbTreeTable.setRemark("");
                dbTreeTable.setType("table");
                dbTreeTable.setLevel("table");
                dbTreeTable.setRef(RefUtils.encode(Lists.newArrayList("", null, idx)));
                dbTreeTables.add(dbTreeTable);
            });
        }
        return dbTreeTables;
    }

    @Override
    public List<TableField> getFields(String catalog, String schema, String tableName) throws Exception {
        return esService.getFields(tableName);
    }

    @Override
    public void afterRead(FlinkActionParam param) {
    }
}
