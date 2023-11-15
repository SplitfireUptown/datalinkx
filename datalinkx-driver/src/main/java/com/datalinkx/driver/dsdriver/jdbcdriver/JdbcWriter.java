package com.datalinkx.driver.dsdriver.jdbcdriver;

import com.datalinkx.driver.dsdriver.base.column.MetaColumn;
import com.datalinkx.driver.dsdriver.base.column.WriterConnection;
import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class JdbcWriter extends AbstractWriter {
    String username;
    String password;

    List<String> column;
    List<WriterConnection> connection;

    String writeMode;
    String insertSqlMode;
    List<String> preSql;
}
