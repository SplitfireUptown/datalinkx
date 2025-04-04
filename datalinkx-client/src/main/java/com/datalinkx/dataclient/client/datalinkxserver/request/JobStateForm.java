package com.datalinkx.dataclient.client.datalinkxserver.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import retrofit2.http.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStateForm {
    @Field("jobId")
    private String jobId;
    /* 0:运行中, 1:失败, 2:成功, 3:手动停止 */
    @Field("jobStatus")
    private Integer jobStatus;
    @Field("startTime")
    private Long startTime;
    @Field("endTime")
    private Long endTime;
    @Field("errmsg")
    private String errmsg;
    @Field("checkpoint")
    private String checkpoint;
    @Field("appendCount")
    private Integer appendCount = 0;
    @Field("filterCount")
    private Integer filterCount = 0;
    @Field("allCount")
    private Integer allCount = 0;
}
