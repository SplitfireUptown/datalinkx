package com.datalinkx.dataserver.bean.bo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

public class AlarmRuleBo {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class ListBo {
        @JsonProperty("rule_id")
        private String ruleId;
        @JsonProperty("rule_name")
        private String ruleName;
        private Integer type;
        @JsonProperty("alarm_id")
        private String alarmId;
        @JsonProperty("alarm_component_name")
        private String alarmComponentName;
        @JsonProperty("job_id")
        private String jobId;
        @JsonProperty("job_name")
        private String jobName;
        @JsonProperty("push_time")
        private Timestamp pushTime;
        private Integer status;
    }
}
