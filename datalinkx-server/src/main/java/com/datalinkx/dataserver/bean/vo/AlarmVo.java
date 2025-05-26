package com.datalinkx.dataserver.bean.vo;

import com.datalinkx.dataserver.bean.dto.AlarmRuleDto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

public class AlarmVo {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AlarmInfoVo<T extends AlarmRuleDto.AlarmConfig> {
        @JsonProperty("alarm_id")
        private String alarmId;
        private Integer type;
        private String name;
        private T config;
    }


    @Data
    @AllArgsConstructor
    public static final class AlarmRuleVo {
        @JsonProperty("rule_id")
        private String ruleId;
        @JsonProperty("rule_name")
        private String ruleName;
        private Integer type;
        private String alarmId;
        @JsonProperty("alarm_component_name")
        private String alarmComponentName;
        @JsonProperty("job_name")
        private String jobName;
        @JsonProperty("push_time")
        private Timestamp pushTime;
        private Integer status;
    }

}
