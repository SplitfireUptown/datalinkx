package com.datalinkx.rpc.client.xxljob.request;

import lombok.*;
import lombok.experimental.FieldNameConstants;

/**
 * @author: uptown
 * @date: 2025/4/4 14:21
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@ToString(callSuper = true)
public class XxlJobGroupParam {
    private String appname;
    private String title;
    @Builder.Default
    private Integer addressType = 0;
    private String addressList;
}
