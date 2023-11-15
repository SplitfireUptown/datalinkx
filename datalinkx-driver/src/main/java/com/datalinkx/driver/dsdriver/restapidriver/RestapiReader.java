package com.datalinkx.driver.dsdriver.restapidriver;

import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class RestapiReader extends AbstractReader {

    // 暂时不需要
}
