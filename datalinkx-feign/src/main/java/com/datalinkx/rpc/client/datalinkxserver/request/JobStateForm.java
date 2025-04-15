package com.datalinkx.rpc.client.datalinkxserver.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobStateForm {
    private String jobId;
    /* 0:运行中, 1:失败, 2:成功, 3:手动停止 */
    private Integer jobStatus;
    private Long startTime;
    private Long endTime;
    private String errmsg;
    private String checkpoint;
    private Long readCount = 0L;
    private Long writeCount = 0L;
}
