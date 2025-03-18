package com.datalinkx.datajob.bean;

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
    String jobId;
    /* 0:运行中, 1:失败, 2:成功, 3:手动停止 */
    @Field("jobStatus")
    Integer jobStatus;
    @Field("startTime")
    Long startTime;
    @Field("endTime")
    Long endTime;
    @Field("errmsg")
    String errmsg;
    @Field("checkpoint")
    String checkpoint;
    @Field("appendCount")
    private Integer appendCount = 0;
    @Field("filterCount")
    private Integer filterCount = 0;
    @Field("allCount")
    private Integer allCount = 0;
}
