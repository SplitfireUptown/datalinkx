package com.datalinkx.driver.dsdriver.oracledriver;

import com.datalinkx.driver.dsdriver.jdbcdriver.JdbcWriter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class OracleWriter extends JdbcWriter {
}
