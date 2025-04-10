package com.datalinkx.datajob.bean;

import lombok.*;
import lombok.experimental.FieldNameConstants;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@ToString(callSuper = true)
public class XxlJobParam {
    String jobId;
}
