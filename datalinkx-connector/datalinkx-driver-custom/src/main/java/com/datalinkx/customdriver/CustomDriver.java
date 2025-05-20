package com.datalinkx.customdriver;


import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcDriver;
import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcReader;
import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcWriter;
import com.datalinkx.driver.dsdriver.setupinfo.MysqlSetupInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Set;


@Slf4j
public class CustomDriver extends JdbcDriver<MysqlSetupInfo, JdbcReader, JdbcWriter> {


    public CustomDriver(String connectId) {
        super(connectId);
    }

    private static final String CLICKHOUSE_DATABASE_JDBC_PATTERN = "jdbc:clickhouse://%s:%s/%s";
    private static final String CLICKHOUSE_DRIVER_CLASS = "ru.yandex.clickhouse.ClickHouseDriver";
    private static final Set<String> DATE_TYPE_SET = new HashSet<>();

    static {
        DATE_TYPE_SET.add("datetime");
        DATE_TYPE_SET.add("date");
        DATE_TYPE_SET.add("timestamp");
        DATE_TYPE_SET.add("time");
    }

    @Override
    protected String driverClass() {
        return CLICKHOUSE_DRIVER_CLASS;
    }

    @Override
    protected String jdbcUrl() {
        return String.format(
                CLICKHOUSE_DATABASE_JDBC_PATTERN,
                this.jdbcSetupInfo.getServer(),
                this.jdbcSetupInfo.getPort(),
                this.jdbcSetupInfo.getDatabase()
        );
    }
}
