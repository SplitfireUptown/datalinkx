package com.datalinkx.driver.dsdriver.base.reader;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Getter
public abstract class AbstractReader {
    public static final int DEFAULT_FETCH_SIZE = 20000;
}
