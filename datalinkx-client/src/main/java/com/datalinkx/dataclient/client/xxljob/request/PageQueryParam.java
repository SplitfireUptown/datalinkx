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
public class PageQueryParam {
    @Field("start")
    private int start;
    @Field("length")
    private int length;
    @Field("appname")
    private String appname;
    @Field("title")
    private String title;
}
