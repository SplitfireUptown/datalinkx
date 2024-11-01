package com.datalinkx.compute.transform;

import com.datalinkx.compute.model.TransformNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author: uptown
 * @date: 2024/10/27 18:00
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SQLNode extends TransformNode {
    @JsonProperty("source_table_name")
    private String sourceTableName;
    @JsonProperty("result_table_name")
    private String resultTableName;
    private String query;
}
