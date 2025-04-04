package com.datalinkx.dataclient.client.xxljob.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import retrofit2.http.Field;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LogQueryParam {
    @Field("start")
    private int start;
    @Field("length")
    private int length;
    @Field("jobGroup")
    private int jobGroup;
    @Field("logStatus")
    private int logStatus;
    @Field("jobId")
    private int jobId;
}
