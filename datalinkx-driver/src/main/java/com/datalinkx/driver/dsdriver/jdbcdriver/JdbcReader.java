package com.datalinkx.driver.dsdriver.jdbcdriver;

import java.util.List;

import com.datalinkx.driver.dsdriver.base.column.MetaColumn;
import com.datalinkx.driver.dsdriver.base.column.ReaderConnection;
import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
//@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class JdbcReader extends AbstractReader {
    String username;
    String password;

    String where;
    int fetchSize;
    int queryTimeOut;

    List<MetaColumn> column;
    List<ReaderConnection> connection;
}
