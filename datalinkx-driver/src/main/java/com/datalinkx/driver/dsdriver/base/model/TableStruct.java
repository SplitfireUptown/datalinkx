package com.datalinkx.driver.dsdriver.base.model;

import lombok.Data;

import java.util.List;

@Data
public class TableStruct {
    String id;
    String name;
    String realName;
    String remark;

    List<TableField> fieldList;
}
