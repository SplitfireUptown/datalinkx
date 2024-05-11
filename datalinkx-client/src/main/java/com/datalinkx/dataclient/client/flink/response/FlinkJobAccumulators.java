package com.datalinkx.dataclient.client.flink.response;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FlinkJobAccumulators {


    @JsonProperty("user-task-accumulators")
    List<UserTaskAccumulator> userTaskAccumulators;

    @Data
    public static class UserTaskAccumulator {
        private String name;
        private String type;
        private String value;
    }
}
