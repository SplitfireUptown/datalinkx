package com.datalinkx.rpc.client.flink.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

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
