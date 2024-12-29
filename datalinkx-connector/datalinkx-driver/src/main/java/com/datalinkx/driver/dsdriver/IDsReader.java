package com.datalinkx.driver.dsdriver;


import java.util.List;

import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.compute.connector.jdbc.TransformNode;
import com.datalinkx.driver.dsdriver.base.model.DbTableField;
import com.datalinkx.driver.dsdriver.base.model.DbTree;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;
import org.apache.commons.lang3.StringUtils;


public interface IDsReader extends IDsDriver {
    // ============= Flinkx 引擎
    String retrieveMax(FlinkActionMeta param, String field) throws Exception;
    Object getReaderInfo(FlinkActionMeta param) throws Exception;
    List<DbTree.DbTreeTable> treeTable(String catalog, String schema) throws Exception;
    List<DbTableField> getFields(String catalog, String schema, String tableName) throws Exception;

    default Boolean judgeIncrementalField(String catalog, String schema, String tableName, String field) throws Exception {
        return false;
    }
    default String genWhere(FlinkActionMeta unit) throws Exception {

        if (unit.getReader().getSync().getSyncCondition() != null) {
            // 1、获取增量字段
            String field = unit.getReader().getSync().getSyncCondition().getField();
            // 2、获取增量条件
            String fieldType = unit.getReader().getSync().getSyncCondition().getFieldType();

            IDsReader readDsDriver = DsDriverFactory.getDsReader(unit.getReader().getConnectId());
            // 3、如果不是首次增量同步，取上一次同步字段最大值
            if (!StringUtils.isEmpty(unit.getReader().getMaxValue())) {
                String maxValue = unit.getReader().getMaxValue();

                // 3.1、更新下一次的增量条件
                String nextMaxValue = readDsDriver.retrieveMax(unit, field);
                if (!ObjectUtils.isEmpty(nextMaxValue)) {
                    unit.getReader().setMaxValue(nextMaxValue);
                }

                return String.format(" %s %s %s ", wrapColumnName(field),
                        unit.getReader().getSync().getSyncCondition().getStart().getOperator(),
                        wrapValue(fieldType, maxValue));
            }
            // 4、如果是首次增量同步，上一次同步字段最大值为空，保存到下次
            String nextMaxValue = readDsDriver.retrieveMax(unit, field);
            if (!ObjectUtils.isEmpty(nextMaxValue)) {
                unit.getReader().setMaxValue(nextMaxValue);
            }
        }
        return " (1=1) ";
    }

    // ============= Seatunnel引擎
    // 构造seatunnel引擎读信息
    default TransformNode getSourceInfo(FlinkActionMeta unit) throws Exception {
        return null;
    }

    // 构造seatunnel引擎source中sql
    default String transferSourceSQL(FlinkActionMeta unit) {
        return "";
    }
}
