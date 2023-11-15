package com.datalinkx.dataio.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import retrofit2.http.Field;

/**
 * @author uptown
 * @Description TODO
 * @createTime 2021年06月05日 14:05:35
 */
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
