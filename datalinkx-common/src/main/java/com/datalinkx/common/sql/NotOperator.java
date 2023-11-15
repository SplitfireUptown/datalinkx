package com.datalinkx.common.sql;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotOperator extends SqlOperator {
    SqlOperator sqlOperator;
    @Override
    public String generate() {
        return String.format(" (!(%s)) ", sqlOperator.generate());
    }
}
