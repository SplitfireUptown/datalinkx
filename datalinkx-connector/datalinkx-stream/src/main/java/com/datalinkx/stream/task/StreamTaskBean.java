package com.datalinkx.stream.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class StreamTaskBean {
    @JsonProperty("job_id")
    private String jobId;
    @JsonProperty("task_id")
    private String taskId;
    private Integer status;
}
