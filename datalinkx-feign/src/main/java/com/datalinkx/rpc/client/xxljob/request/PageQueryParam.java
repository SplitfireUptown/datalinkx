package com.datalinkx.rpc.client.xxljob.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PageQueryParam {
    private int start;
    private int length;
    private String appname;
    private String title;
}
