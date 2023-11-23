package com.datalinkx.dataserver.client.xxljob.request;

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
    int start;
    @Field("length")
    int length;
    @Field("jobGroup")
    int jobGroup;
    @Field("logStatus")
    int logStatus;
    @Field("jobId")
    int jobId;
}
