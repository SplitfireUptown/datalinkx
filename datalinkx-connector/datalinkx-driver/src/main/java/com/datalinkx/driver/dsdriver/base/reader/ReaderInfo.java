package com.datalinkx.driver.dsdriver.base.reader;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReaderInfo<T> {
    String name;
    T parameter;
}
