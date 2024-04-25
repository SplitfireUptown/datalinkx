package com.datalinkx.driver.dsdriver;


import java.util.ArrayList;
import java.util.List;

import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import org.apache.commons.lang3.StringUtils;

public interface IDsDriver {
    Object connect(boolean check) throws Exception;
    String getConnectId();

    default String wrapColumnName(String columnName) {
        return columnQuota() + columnName + columnQuota();
    }

    default String wrapValue(String fieldType, String value) {
        return valueQuota() + value + valueQuota();
    }

    default String valueQuota() {
        return "";
    }

    default String columnQuota() {
        return "";
    }


//
//    default String wrapTableName(String catalog, String schema, String tableName) {
//        List<String> fullName = new ArrayList<>();
//        if (StringUtils.isNotEmpty(catalog)) {
//            fullName.add(String.format("%s%s%s", columnQuota(), catalog, columnQuota()));
//        }
//
//        if (StringUtils.isNotEmpty(schema)) {
//            fullName.add(String.format("%s%s%s", columnQuota(), schema, columnQuota()));
//        }
//
//        if (StringUtils.isNotEmpty(tableName)) {
//            fullName.add(String.format("%s%s%s", columnQuota(), tableName, columnQuota()));
//        }
//        return String.join(".", fullName);
//    }
}
