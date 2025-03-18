package com.datalinkx.driver.dsdriver.base.writer;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public abstract class AbstractWriter {
    Integer batchSize;
}
