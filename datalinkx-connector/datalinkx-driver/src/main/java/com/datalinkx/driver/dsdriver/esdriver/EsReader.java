package com.datalinkx.driver.dsdriver.esdriver;

import java.util.List;

import com.datalinkx.driver.dsdriver.base.column.MetaColumn;
import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@EqualsAndHashCode(callSuper = true)
@Data
//@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class EsReader extends AbstractReader {
    private String address;
    private JsonNode query;
    private String index;
    private String[] type;
    private Long batchSize;
    private String username;
    private String password;
    private Long timeout;
    List<MetaColumn> column;
}
