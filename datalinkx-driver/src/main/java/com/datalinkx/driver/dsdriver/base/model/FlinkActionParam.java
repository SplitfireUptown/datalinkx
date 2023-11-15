package com.datalinkx.driver.dsdriver.base.model;

import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.Map;
import java.util.stream.Collectors;


@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlinkActionParam {
    String jobId;
    String taskId;
    DataTransJobDetail.Reader reader;
    DataTransJobDetail.Writer writer;
    Integer full;

    int readRecords = 0;
    int writeRecords = 0;
    int errorRecords = 0;
    String errorMsg = "success";

    IDsReader dsReader;
    IDsWriter dsWriter;

    long writeBytes;


    public String getReaderFieldType(String fieldName) {
        Map<String, DataTransJobDetail.Column> columnTypeMap = this.getReader().getColumns().stream()
                .collect(Collectors.toMap(DataTransJobDetail.Column::getName, x -> x));

        return columnTypeMap.get(fieldName).getType();
    }
}
