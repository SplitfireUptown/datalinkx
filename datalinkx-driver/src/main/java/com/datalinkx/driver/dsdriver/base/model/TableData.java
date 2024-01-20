package com.datalinkx.driver.dsdriver.base.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TableData {
    private List<TableField> fields;
    private List<List<String>> data;
}
