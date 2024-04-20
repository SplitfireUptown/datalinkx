package com.datalinkx.driver.dsdriver.base.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class EngineActionMeta {
    // 流转任务id
    String jobId;
    // 底层引擎侧任务id
    String taskId;
    // 是否覆盖 0 - 否 1 - 是
    Integer cover;
    // 数据总读行数
    int readRecords = 0;
    // 数据总写行数 = readRecords - errorRecords
    int writeRecords = 0;
    // 数据失败行数
    int errorRecords = 0;
    // 写字节数
    long writeBytes;
}
