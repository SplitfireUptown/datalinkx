package com.datalinkx.datajob.bean;

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
    @JsonProperty("filterCount")
    private Integer filterCount = 0;
    @JsonProperty("allCount")
    private Integer allCount = 0;
}
