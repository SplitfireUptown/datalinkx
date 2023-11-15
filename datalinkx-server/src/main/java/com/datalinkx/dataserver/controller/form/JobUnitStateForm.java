package com.datalinkx.dataserver.controller.form;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author uptown
 * @Description TODO
 * @createTime 2021年06月03日 20:58:23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobUnitStateForm {
    private String jobId;
    private String jobTaskId;
    private String fromTableId;
    private String toTableId;
    private Integer tableStatus;
    private Long startTime;
    private Long endTime;

    private Integer appendCount = 0;
    private Integer updateCount = 0;
    private Integer deleteCount = 0;
    private Integer failedCount = 0;
    private Integer allCount = 0;

    private String errorMsg = "";

    private Long writeBytes;
}
