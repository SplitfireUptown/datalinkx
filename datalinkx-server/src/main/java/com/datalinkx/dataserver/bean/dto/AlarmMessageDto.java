package com.datalinkx.dataserver.bean.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AlarmMessageDto {
    @JsonProperty("job_id")
    private String jobId;
    private Integer status;
    private String errmsg;
}
