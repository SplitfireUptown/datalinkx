package com.datalinkx.driver.dsdriver.base.model;

import java.util.Map;
import java.util.stream.Collectors;

import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

/**
 * 执行引擎任务类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlinkActionParam {
    // 流转任务id
    String jobId;
    // flink任务id
    String taskId;
    // datalinkx-server业务来源库信息
    DataTransJobDetail.Reader reader;
    // datalinkx-server业务目标库信息
    DataTransJobDetail.Writer writer;
    // 是否覆盖 0 - 否 1 - 是
    Integer cover;
    // 数据总读行数
    int readRecords = 0;
    // 数据总写行数 = readRecords - errorRecords
    int writeRecords = 0;
    // 数据失败行数
    int errorRecords = 0;
    // 来源库driver驱动
    IDsReader dsReader;
    // 目标库driver驱动
    IDsWriter dsWriter;
    // 写字节数
    long writeBytes;


    public String getReaderFieldType(String fieldName) {
        Map<String, DataTransJobDetail.Column> columnTypeMap = this.getReader().getColumns().stream()
                .collect(Collectors.toMap(DataTransJobDetail.Column::getName, x -> x));
        return columnTypeMap.get(fieldName).getType();
    }
}
