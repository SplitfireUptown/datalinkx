package com.datalinkx.driver.dsdriver.base.column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WriterConnection {
    public static final String GP_JDBC_PATTERN = "jdbc:pivotal:greenplum://%s;DatabaseName=%s";
    public static final String MYSQL_JDBC_PATTERN = "jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=utf8";
    private static final String ORACLE_BASIC_SID_JDBC_PATTERN = "jdbc:oracle:thin:@%s:%s";

    public static final Map<String, String> TYPE_PATTERN = new HashMap<>();
    static {
        TYPE_PATTERN.put("mysql", MYSQL_JDBC_PATTERN);
        TYPE_PATTERN.put("greenplum", GP_JDBC_PATTERN);
        TYPE_PATTERN.put("oracle", ORACLE_BASIC_SID_JDBC_PATTERN);
    }

    String schema;
    String jdbcUrl;
    List<String> table;
}
