package com.datalinkx.compute.connector.jdbc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @author: uptown
 * @date: 2024/10/27 19:10
 */
@SuperBuilder(toBuilder = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PluginNode {
    @JsonProperty("plugin_name")
    private String pluginName;
}
