package com.datalinkx.dataserver.bean.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

public class AlarmRuleDto {
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class ListDto {
        @JsonProperty("job_id")
        private List<String> jobIds = new ArrayList<>();
        @JsonProperty("alarm_id")
        private List<String> alarmIds = new ArrayList<>();
    }



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @SuperBuilder(toBuilder = true)
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, visible = true, property = "type")
    @JsonSubTypes(value = {
            @JsonSubTypes.Type(value = AlarmEmailDto.class, name = "0"),
            @JsonSubTypes.Type(value = AlarmDingTalkDto.class, name = "1")
    })
    public static class AlarmConfig {
        private Integer type;
        private List<String> content;
    }


    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class AlarmEmailDto extends AlarmConfig {
        private String host;
        private Integer port;
        private String from;
        private String to;
        private String user;
        private String password;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class AlarmDingTalkDto extends AlarmConfig {
        private String webhook;
        private String secret;
    }
}
