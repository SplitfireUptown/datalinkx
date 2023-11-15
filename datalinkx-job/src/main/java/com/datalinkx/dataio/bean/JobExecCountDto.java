package com.datalinkx.dataio.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JobExecCountDto {
    @JsonProperty("appendCount")
    private Integer appendCount = 0;
    @JsonProperty("updateCount")
    private Integer updateCount = 0;
    @JsonProperty("deleteCount")
    private Integer deleteCount = 0;
    @JsonProperty("failedCount")
    private Integer failedCount = 0;
    @JsonProperty("allCount")
    private Integer allCount = 0;
}
