package com.datalinkx.compute.transform;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.compute.connector.model.SQLNode;

/**
 * @author: uptown
 * @date: 2024/11/17 17:33
 */
public class SQLTransformDriver implements ITransformDriver {

    public SQLTransformDriver() {
    }

    @Override
    public Object transferInfo(String transferMeta) {
        return SQLNode.builder()
                .query(transferMeta)
                .sourceTableName(MetaConstants.CommonConstant.SOURCE_TABLE)
                .resultTableName(MetaConstants.CommonConstant.RESULT_TABLE)
                .build();
    }
}
