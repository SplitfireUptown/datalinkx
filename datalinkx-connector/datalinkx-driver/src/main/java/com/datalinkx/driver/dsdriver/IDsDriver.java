package com.datalinkx.driver.dsdriver;


import java.util.ArrayList;
import java.util.List;

import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import org.apache.commons.lang3.StringUtils;

public interface IDsDriver {
    Object connect(boolean check) throws Exception;
    String getConnectId();

    default String wrapColumnName(String columnName) {
        return columnQuota() + columnName + columnQuota();
    }

    default String wrapValue(String fieldType, String value) {
        return valueQuota() + value + valueQuota();
    }

    default String valueQuota() {
        return "";
    }

    default String columnQuota() {
        return "";
    }


    default String genWhere(FlinkActionMeta unit) throws Exception {

        if (unit.getReader().getSync().getSyncCondition() != null) {
            // 1、获取增量字段
            String field = unit.getReader().getSync().getSyncCondition().getField();
            // 2、获取增量条件
            String fieldType = unit.getReader().getSync().getSyncCondition().getFieldType();

            // 3、如果不是首次增量同步，取上一次同步字段最大值
            if (!StringUtils.isEmpty(unit.getReader().getMaxValue())) {
                String maxValue = unit.getReader().getMaxValue();

                // 3.1、更新下一次的增量条件
                String nextMaxValue = unit.getDsReader().retrieveMax(unit, field);
                if (!ObjectUtils.isEmpty(nextMaxValue)) {
                    unit.getReader().setMaxValue(nextMaxValue);
                }

                return String.format(" %s %s %s ", wrapColumnName(field),
                        unit.getReader().getSync().getSyncCondition().getStart().getOperator(),
                        wrapValue(fieldType, maxValue));
            }
            // 4、如果是首次增量同步，上一次同步字段最大值为空，保存到下次
            String nextMaxValue = unit.getDsReader().retrieveMax(unit, field);
            if (!ObjectUtils.isEmpty(nextMaxValue)) {
                unit.getReader().setMaxValue(nextMaxValue);
            }
        }
        return " (1=1) ";
    }

//
//    default String wrapTableName(String catalog, String schema, String tableName) {
//        List<String> fullName = new ArrayList<>();
//        if (StringUtils.isNotEmpty(catalog)) {
//            fullName.add(String.format("%s%s%s", columnQuota(), catalog, columnQuota()));
//        }
//
//        if (StringUtils.isNotEmpty(schema)) {
//            fullName.add(String.format("%s%s%s", columnQuota(), schema, columnQuota()));
//        }
//
//        if (StringUtils.isNotEmpty(tableName)) {
//            fullName.add(String.format("%s%s%s", columnQuota(), tableName, columnQuota()));
//        }
//        return String.join(".", fullName);
//    }
}
