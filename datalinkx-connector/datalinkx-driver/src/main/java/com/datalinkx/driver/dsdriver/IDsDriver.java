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
}
