package com.datalinkx.compute.transform;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.compute.connector.model.SQLNode;
import com.datalinkx.compute.connector.jdbc.TransformNode;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/11/17 17:33
 */
public class SQLTransformDriver extends ITransformDriver {


    @Override
    public TransformNode transferInfo(Map<String, Object> commonSettings, String transferSQLMeta) {
        return SQLNode.builder()
                .query(transferSQLMeta)
                .sourceTableName(MetaConstants.CommonConstant.SOURCE_TABLE)
                .resultTableName(MetaConstants.CommonConstant.SQL_OUTPUT_TABLE)
                .pluginName("sql")
                .build();
    }

    // 分成了三部分select、from where
    @Override
    public String analysisTransferMeta(JsonNode nodeMeta) {
        JsonNode dataMeta = nodeMeta.get("data");
        String sql = String.format("select %s from %s", dataMeta.get("sqlOperatorValue").asText(), dataMeta.get("sqlOperatorFrom").asText());
        if (ObjectUtils.isEmpty(dataMeta.get("sqlOperatorWhereValue"))) {
            sql += String.format(" where %s", dataMeta.get("sqlOperatorWhereValue").asText());
        }

        if (ObjectUtils.isEmpty(dataMeta.get("sqlOperatorGroupValue"))) {
            sql += String.format(" group by %s", dataMeta.get("sqlOperatorGroupValue").asText());
        }
        return sql;
    }
}
