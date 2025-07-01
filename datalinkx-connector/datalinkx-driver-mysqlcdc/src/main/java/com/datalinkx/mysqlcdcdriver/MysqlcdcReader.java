package com.datalinkx.mysqlcdcdriver;

import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class MysqlcdcReader extends AbstractReader {
    private List<String> table;
    private String username;
    private String password;
    private String database;
    private Integer port;
    private String cat;
    private String host;
    private String jdbcUrl;
    private Boolean pavingData;
}
