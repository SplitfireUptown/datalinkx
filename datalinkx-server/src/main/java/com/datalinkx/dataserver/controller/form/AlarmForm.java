package com.datalinkx.dataserver.controller.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

public class AlarmForm {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AlarmCreateForm {
        private String name;
        private Integer type;
        private String config;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class AlarmModifyForm extends AlarmCreateForm {
        private String alarmId;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RuleListForm  {
        @JsonProperty("component_name")
        private String componentName;
        @JsonProperty("job_name")
        private String jobName;

        public String getComponentName() {
            return componentName + "%";
        }

        public String getJobName() {
            return jobName + "%";
        }
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RuleCreateForm {
        private String name;
        @JsonProperty("alarm_id")
        private String alarmId;
        @JsonProperty("job_id")
        private String jobId;
        private Integer type;
        private Integer status = 0;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RuleModifyForm extends RuleCreateForm {
        @JsonProperty("rule_id")
        private String ruleId;
    }
}
