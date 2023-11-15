package com.datalinkx.driver.dsdriver.base.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TableId {
    String folderId;
    String tableId;
    String realTableName;

    List<TableField> fieldList;
}
