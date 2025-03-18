package com.datalinkx.driver.dsdriver.base.writer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WriterInfo<T> {
    String name;
    T parameter;
}
