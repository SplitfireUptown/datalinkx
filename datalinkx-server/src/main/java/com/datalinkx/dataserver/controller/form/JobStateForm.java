package com.datalinkx.dataserver.controller.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStateForm {
    String jobId;
    String jobTaskId;
    Integer jobStatus;
    Long startTime;
    Long endTime;
    String errmsg;
    private Long appendCount = 0L;
    private Long allCount = 0L;
    private Long filterCount = 0L;
}
