package com.datalinkx.esdriver;


import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class EsWriter extends AbstractWriter {
    private String address;
    private String username;
    private String password;
    private String index;
    private String type;
    private Long bulkAction;
    private Long timeout;
    private List<Map<String, Object>> idColumn;
    private List<String> column;
}
