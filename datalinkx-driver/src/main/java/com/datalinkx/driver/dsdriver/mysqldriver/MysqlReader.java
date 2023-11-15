package com.datalinkx.driver.dsdriver.mysqldriver;

import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcReader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@EqualsAndHashCode(callSuper = true)
@Getter
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class MysqlReader extends JdbcReader {
}
