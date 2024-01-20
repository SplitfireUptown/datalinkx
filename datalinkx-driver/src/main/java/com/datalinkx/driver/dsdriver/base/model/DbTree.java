package com.datalinkx.driver.dsdriver.base.model;

import java.util.List;

import lombok.Data;

@Data
public class DbTree {
    String name;
    // catalog, schema, table
    String level;
    String ref;
    List<DbTreeTable> table;
    List<DbTree> folder;

    @Data
    public static class DbTreeTable extends DbTree {
        String remark;
        String type;
    }
}
