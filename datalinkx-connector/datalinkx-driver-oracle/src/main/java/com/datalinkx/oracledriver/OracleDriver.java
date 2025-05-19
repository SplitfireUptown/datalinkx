package com.datalinkx.oracledriver;

import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcDriver;
import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcReader;
import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcWriter;
import com.datalinkx.driver.dsdriver.setupinfo.OracleSetupInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;


@Slf4j
public class OracleDriver extends JdbcDriver<OracleSetupInfo, JdbcReader, JdbcWriter> {

    private static final String ORACLE_BASIC_SID_JDBC_PATTERN = "jdbc:oracle:thin:@%s:%s";
    private static final String ORACLE_BASIC_SERVER_NAME_JDBC_PATTERN = "jdbc:oracle:thin:@//%s/%s";
    private static final String ALIAS_SID = "SID";
    private static final String ALIAS_SERVER_NAME = "SERVERNAME";
    private static final String DATE_FORMAT_PATTERN = "to_date('%S', 'YYYY-MM-DD HH24:MI:SS')";
    private static final String ORACLE_DRIVER_CLASS = "oracle.jdbc.driver.OracleDriver";

    private static final Set<String> DATE_TYPE_SET = new HashSet<>();

    static {
        DATE_TYPE_SET.add("datetime");
        DATE_TYPE_SET.add("date");
        DATE_TYPE_SET.add("timestamp");
    }

    public OracleDriver(String connectId) {
        super(connectId);
    }

    @Override
    protected String jdbcUrl() {
        String jdbc;
        String url = this.jdbcSetupInfo.getServer() + ":" + jdbcSetupInfo.getPort();
        String sidOrServerName = jdbcSetupInfo.getSid();

        String pattern = (ALIAS_SID.equalsIgnoreCase(jdbcSetupInfo.getConnectType())) ? ORACLE_BASIC_SID_JDBC_PATTERN : ORACLE_BASIC_SERVER_NAME_JDBC_PATTERN;
        jdbc = String.format(pattern, url, sidOrServerName);
        return jdbc;
    }

    @Override
    protected String driverClass() {
        return ORACLE_DRIVER_CLASS;
    }

    @Override
    public String columnQuota() {
        return "\"";
    }

    @Override
    public String wrapValue(String fieldType, String value) {
        if (DATE_TYPE_SET.contains(fieldType.toLowerCase())) {
            return String.format(DATE_FORMAT_PATTERN, value.split("\\.")[0]);
        } else if ("String".equalsIgnoreCase(fieldType)) {
            return super.wrapValue(fieldType, value);
        } else {
            return value;
        }
    }

    @Override
    protected Properties connectProp() {
        Properties properties = super.connectProp();
        properties.setProperty("remarksReporting", "true");
        properties.setProperty("remarks", "true");
        return properties;
    }
}
