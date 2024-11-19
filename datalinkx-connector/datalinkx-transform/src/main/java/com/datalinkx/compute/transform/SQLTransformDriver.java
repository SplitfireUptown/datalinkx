package com.datalinkx.compute.transform;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.compute.connector.model.SQLNode;
import com.datalinkx.compute.connector.model.TransformNode;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: uptown
 * @date: 2024/11/17 17:33
 */
public class SQLTransformDriver implements ITransformDriver {

    public SQLTransformDriver() {
    }

    @Override
    public TransformNode transferInfo(String transferSQLMeta) {
        return SQLNode.builder()
                .query(transferSQLMeta)
                .sourceTableName(MetaConstants.CommonConstant.SOURCE_TABLE)
                .resultTableName(MetaConstants.CommonConstant.RESULT_TABLE)
                .build();
    }

    // 分成了三部分select、from where
    @Override
    public String analysisTransferMeta(JsonNode nodeMeta) {
        Set<String> finalSqlArray = new HashSet<>();
        JsonNode dataMeta = nodeMeta.get("data");
        String sql = String.format("select %s from %s", dataMeta.get(0), dataMeta.get(1));
        if (ObjectUtils.isEmpty(dataMeta.get(2))) {
            sql += String.format(" where %s", dataMeta.get(2));
        }
        return sql;
    }
}
