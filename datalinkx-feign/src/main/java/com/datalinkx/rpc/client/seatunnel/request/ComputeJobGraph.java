package com.datalinkx.rpc.client.seatunnel.request;

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
public class ComputeJobGraph {
    @JsonProperty("job_id")
    private String jobId;
    private Map<String, Object> env;
    private List<Object> source;
    private List<Object> transform;
    private List<Object> sink;
}
