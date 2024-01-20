package com.datalinkx.driver.dsdriver.base.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableId {
    String folderId;
    String tableId;
    String realTableName;

    List<TableField> fieldList;
}
