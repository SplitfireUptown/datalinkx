package com.datalinkx.redisdriver;

import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RedisWriter extends AbstractWriter {
    private String hostPort;
    private String type;
    private String mode;
    private String customKey;
    private Integer[] keyIndexes;
    private String password;
    private Integer database;
}
