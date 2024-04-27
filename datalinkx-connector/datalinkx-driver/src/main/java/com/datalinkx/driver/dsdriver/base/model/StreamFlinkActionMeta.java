package com.datalinkx.driver.dsdriver.base.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

/**
 * @author: uptown
 * @date: 2024/4/27 17:15
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class StreamFlinkActionMeta extends EngineActionMeta {
    String readerDsInfo;
    String writerDsInfo;
    String checkpoint;
}
