package com.datalinkx.datajob.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import retrofit2.http.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSyncModeForm {
    @Field("job_id")
    private String jobId;
    @JsonProperty("increate_value")
    private String increateValue;
}
