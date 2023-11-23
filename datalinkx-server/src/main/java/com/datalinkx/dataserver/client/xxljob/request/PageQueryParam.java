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
public class PageQueryParam {
    @Field("start")
    int start;
    @Field("length")
    int length;
    @Field("jobGroup")
    int jobGroup;
    @Field("triggerStatus")
    int triggerStatus;
    @Field("jobDesc")
    String jobDesc;
    @Field("executorHandler")
    String executorHandler;
    @Field("author")
    String author;
}
