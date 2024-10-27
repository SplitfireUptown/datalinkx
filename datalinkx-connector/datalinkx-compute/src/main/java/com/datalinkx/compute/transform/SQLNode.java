package com.datalinkx.compute.transform;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author: uptown
 * @date: 2024/10/27 18:00
 */
@Data
public class SQLNode {
    @JsonProperty("source_table_name")
    private String sourceTableName;
    @JsonProperty("result_table_name")
    private String resultTableName;
    private String query;
}
