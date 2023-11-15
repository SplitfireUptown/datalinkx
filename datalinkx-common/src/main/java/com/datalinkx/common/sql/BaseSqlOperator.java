package com.datalinkx.common.sql;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class BaseSqlOperator extends SqlOperator {
    String sql;

    @Override
    public String generate() {
        return " (" + sql + ") ";
    }
}
