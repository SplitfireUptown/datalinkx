package com.datalinkx.driver.dsdriver.base.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableData {
    private List<TableField> fields;
    private List<List<String>> data;
}
