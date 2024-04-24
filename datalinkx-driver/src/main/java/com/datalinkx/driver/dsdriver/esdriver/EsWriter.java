package com.datalinkx.driver.dsdriver.esdriver;


import java.util.List;
import java.util.Map;

import com.datalinkx.driver.dsdriver.base.column.MetaColumn;
import com.datalinkx.driver.dsdriver.base.writer.AbstractWriter;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

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
    private List<MetaColumn> column;
}
