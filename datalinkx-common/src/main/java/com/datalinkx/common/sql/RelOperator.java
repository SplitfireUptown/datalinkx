package com.datalinkx.common.sql;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RelOperator extends SqlOperator {
    String rel;
    private List<SqlOperator> operatorList;

    @Override
    public String generate() {
        if (operatorList.isEmpty()) {
            return " (1=1) ";
        }

        return " ("
                + operatorList.stream().map(SqlOperator::generate).collect(Collectors.joining(" " + rel + " "))
                + ") ";
    }

    public static RelOperator newRelOperator(String rel, SqlOperator... operators) {
        return new RelOperator(rel, Arrays.asList(operators));
    }
}
