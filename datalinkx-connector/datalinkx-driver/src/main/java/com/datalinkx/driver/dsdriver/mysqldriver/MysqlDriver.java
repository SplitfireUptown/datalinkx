package com.datalinkx.driver.dsdriver.mysqldriver;


import java.util.HashSet;
import java.util.Set;

import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcDriver;
import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcReader;
import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcWriter;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class MysqlDriver extends JdbcDriver<MysqlSetupInfo, JdbcReader, JdbcWriter> {

    public MysqlDriver(String connectId) {
        super(connectId);
    }

    private static final String MYSQL_DATABASE_JDBC_PATTERN = "jdbc:mysql://%s:%s/%s?useCursorFetch=%s"
            + "&useUnicode=%s&zeroDateTimeBehavior=%s&characterEncoding=%s&useInformationSchema=%s&serverTimezone=%s&socketTimeout=%s"
            + "&useSSL=false";

    private static final String MYSQL_DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";


    private static final Set<String> DATE_TYPE_SET = new HashSet<>();

    
    static {
        DATE_TYPE_SET.add("datetime");
        DATE_TYPE_SET.add("date");
        DATE_TYPE_SET.add("timestamp");
        DATE_TYPE_SET.add("time");
    }

    @Override
    protected String jdbcUrl() {
        return String.format(
                MYSQL_DATABASE_JDBC_PATTERN,
                this.jdbcSetupInfo.getServer(),
                this.jdbcSetupInfo.getPort(),
                this.jdbcSetupInfo.getDatabase(),
                this.jdbcSetupInfo.getUseCursorFetch(),
                this.jdbcSetupInfo.getUseUnicode(),
                this.jdbcSetupInfo.getZeroDateTimeBehavior(),
                this.jdbcSetupInfo.getCharacterEncoding(),
                this.jdbcSetupInfo.getUseInformationSchema(),
                this.jdbcSetupInfo.getServerTimezone(),
                this.jdbcSetupInfo.getSocketTimeout()
        );
    }



    @Override
    protected String driverClass() {
        return MYSQL_DRIVER_CLASS;
    }

    @Override
    public String columnQuota() {
        return "`";
    }

    @Override
    public String valueQuota() {
        return "'";
    }

    @Override
    public String wrapValue(String fieldType, String value) {
        if ("String".equalsIgnoreCase(fieldType) || DATE_TYPE_SET.contains(fieldType.toLowerCase())) {
            return super.wrapValue(fieldType, value);
        } else {
            return value;
        }
    }
}
