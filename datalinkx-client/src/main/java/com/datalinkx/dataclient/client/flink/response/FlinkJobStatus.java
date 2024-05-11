package com.datalinkx.dataclient.client.flink.response;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class FlinkJobStatus {
    @JsonProperty("name")
    String jobId;
    String state;
    @JsonProperty("start-time")
    Long startTime;
    @JsonProperty("end-time")
    Long endTime;
    Long duration;
    List<Vertice> vertices;

    @Data
    public static class Vertice {
        String id;
        String name;
        Metrics metrics;
    }

    @Data
    public static class Metrics {
        @JsonProperty("read-bytes")
        Long readBytes;
        @JsonProperty("read-bytes-complete")
        Boolean readBytesComplete;

        @JsonProperty("write-bytes")
        Long writeBytes;
        @JsonProperty("write-bytes-complete")
        Boolean writeBytesComplete;

        @JsonProperty("read-records")
        Long readRecords;
        @JsonProperty("read-records-complete")
        Boolean readRecordsComplete;

        @JsonProperty("write-records")
        Long writeRecords;
        @JsonProperty("write-records-complete")
        Boolean writeRecordsComplete;
    }
}
