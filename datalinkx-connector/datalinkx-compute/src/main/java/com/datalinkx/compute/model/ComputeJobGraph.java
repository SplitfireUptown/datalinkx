package com.datalinkx.compute.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.List;
import java.util.Map;

/**
 * @author: uptown
 * @date: 2024/10/27 18:49
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Builder
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputeJobGraph<T extends PluginNode, C extends TransformNode, E extends PluginNode> {
    @JsonProperty("job_id")
    private String jobId;
    private Map<String, Object> env;
    private List<T> source;
    private List<C> transform;
    private List<E> sink;
}
