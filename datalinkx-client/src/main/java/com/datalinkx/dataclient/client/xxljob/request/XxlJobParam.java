package com.datalinkx.dataclient.client.xxljob.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@ToString(callSuper = true)
public class XxlJobParam {
    private String jobId;
}
