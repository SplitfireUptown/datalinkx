package com.datalinkx.common.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegLikeOperator extends SqlOperator {
    public static final String REGLIKE_PATTERN = " REGEXP_LIKE(%s, %s) ";
    String fieldName;
    String compareOp;
    String fieldType;
    String value;
    String realType;

    public RegLikeOperator(String fieldName, String compareOp, String fieldType, String value) {
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

        return String.format(REGLIKE_PATTERN, fieldName, value);
    }
}
