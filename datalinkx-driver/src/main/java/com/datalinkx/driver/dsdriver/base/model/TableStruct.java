package com.datalinkx.driver.dsdriver.base.model;

import java.util.List;

import lombok.Data;

@Data
public class TableStruct {
    String id;
    String name;
    String realName;
    String remark;

    List<TableField> fieldList;
}
