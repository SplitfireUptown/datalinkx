package com.datalinkx.compute.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author: uptown
 * @date: 2024/10/27 19:10
 */
@Data
public class PluginNode {
    @JsonProperty("plugin_name")
    private String pluginName;
}
