package com.datalinkx.driver.dsdriver.base;

import com.datalinkx.common.sql.*;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionParam;
import com.datalinkx.driver.model.DataTransJobDetail;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public abstract class SqlGenerator {
    /**
     * quota for column name
     * @param columnName
     * @return quota for column name
     */
    protected String wrapColumnName(String columnName) {
        return columnQuota() + columnName + columnQuota();
    }

    protected String wrapValue(String fieldType, String value) {
        return valueQuota() + value + valueQuota();
    }

    protected String valueQuota() {
        return "";
    }

    /**
     * 操作子格式
     * @return
     */
    protected String wrapOp(String op, String value) {
        if ("like".equals(op) || "not like".equals(op)) {
            return "%" + value + "%";
        }

        return value;
    }

    protected String columnQuota() {
        return "";
    }

    protected String nowValue() {
        return "now()";
    }

    public SqlOperator genWhere(FlinkActionParam unit) throws Exception {

        if (unit.getReader().getSync().getSyncCondition() != null) {
            // 1、获取增量字段
            String field = unit.getReader().getSync().getSyncCondition().getField();
            // 2、获取增量条件
            String fieldType = unit.getReader().getSync().getSyncCondition().getFieldType();

            // 3、如果不是首次增量同步，取上一次同步字段最大值
            if (!StringUtils.isEmpty(unit.getReader().getMaxValue())) {
                String maxValue = unit.getReader().getMaxValue();
                CompareOperator maxOp = new CompareOperator(
                        wrapColumnName(field),
                        unit.getReader().getSync().getSyncCondition().getStart().getOperator(),
                        fieldType,
                        wrapValue(fieldType, maxValue)
                );

                // 3.1、更新下一次的增量条件
                String nextMaxValue = unit.getDsReader().retrieveMax(unit, field);
                if (!ObjectUtils.isEmpty(nextMaxValue)) {
                    unit.getReader().setMaxValue(nextMaxValue);
                }
                return RelOperator.newRelOperator("and", maxOp);
            }
            // 4、如果是首次增量同步，上一次同步字段最大值为空，保存到下次
            String nextMaxValue = unit.getDsReader().retrieveMax(unit, field);
            if (!ObjectUtils.isEmpty(nextMaxValue)) {
                unit.getReader().setMaxValue(nextMaxValue);
            }
        }

        return new RelOperator("and", new ArrayList<>());
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
