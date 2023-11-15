package com.datalinkx.common.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompareOperator extends SqlOperator {
    public static final String COMPARE_PATTERN = " %s %s %s ";
    String fieldName;
    String compareOp;
    String fieldType;
    String value;
    String realType;

    public CompareOperator(String fieldName, String compareOp, String fieldType, String value) {
        this.fieldName = fieldName;
        this.compareOp = compareOp;
        this.fieldType = fieldType;
        this.value = value;
    }

    @Override
    public String generate() {
        if (StringUtils.isEmpty(value) || StringUtils.isEmpty(compareOp) || StringUtils.isEmpty(fieldName)) {
            return " (1=1) ";
        }

        if ("bit".equalsIgnoreCase(realType)) {
            value = String.format("cast('%s' as bit)", value);
        }

        return String.format(COMPARE_PATTERN, fieldName, compareOp, value);
    }
}
