package com.datalinkx.driver.dsdriver.setupinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MysqlcdcSetupInfo extends MysqlSetupInfo {
    // 监听的操作
    public String cat;
    // 是否将解析出的json数据铺平
    public Boolean pavingData = true;
}
