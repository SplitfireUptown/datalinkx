package com.datalinkx.dataserver.client.xxljob.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import retrofit2.http.Field;

/**
 * @author uptown
 * @createTime 2021年05月21日 17:01:54
 */
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
