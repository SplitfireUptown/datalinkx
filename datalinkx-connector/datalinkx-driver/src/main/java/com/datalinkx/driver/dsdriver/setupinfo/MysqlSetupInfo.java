package com.datalinkx.driver.dsdriver.setupinfo;

import java.util.TimeZone;

import com.datalinkx.common.utils.ObjectUtils;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class MysqlSetupInfo extends JdbcSetupInfo {
    private String useCursorFetch = "true";
    private String useUnicode = "true";
    private String zeroDateTimeBehavior = "round";
    private String characterEncoding = "UTF8";
    private String useInformationSchema = "true";
    private String serverTimezone;
    private String socketTimeout = "300000";

    public String getServerTimezone() {
        if (!ObjectUtils.isEmpty(serverTimezone)) {
            return serverTimezone;
        }
        return serverTimezoneStr();
    }

    public String serverTimezoneStr() {
        int timezoneOffsetHours = TimeZone.getDefault().getRawOffset() / (1000 * 60 * 60);
        if(timezoneOffsetHours < 0) {
            return "GMT%2B" + (-timezoneOffsetHours);
        } else if (timezoneOffsetHours > 0) {
            return "GMT-" + timezoneOffsetHours;
        } else {
            return "GMT";
        }
    }
}
