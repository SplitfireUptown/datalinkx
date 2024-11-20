package com.datalinkx.compute.connector.model;

import com.datalinkx.compute.connector.jdbc.PluginNode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author: uptown
 * @date: 2024/10/27 19:33
 */
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransformNode extends PluginNode {
    @JsonProperty("source_table_name")
    private String sourceTableName;
    @JsonProperty("result_table_name")
    private String resultTableName;
}
