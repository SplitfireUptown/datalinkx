package com.datalinkx.esdriver;

import com.datalinkx.driver.dsdriver.base.reader.AbstractReader;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


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
    List<String> column;
}
