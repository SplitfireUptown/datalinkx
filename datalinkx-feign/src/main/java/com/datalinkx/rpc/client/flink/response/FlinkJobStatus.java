package com.datalinkx.rpc.client.flink.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class FlinkJobStatus {
    @JsonProperty("name")
    private String jobId;
    private String state;
    @JsonProperty("start-time")
    private Long startTime;
    @JsonProperty("end-time")
    private Long endTime;
    private Long duration;
    private List<Vertice> vertices;

    @Data
    public static class Vertice {
        private String id;
        private String name;
        private Metrics metrics;
    }

    @Data
    public static class Metrics {
        @JsonProperty("read-bytes")
        private Long readBytes;
        @JsonProperty("read-bytes-complete")
        private Boolean readBytesComplete;

        @JsonProperty("write-bytes")
        private Long writeBytes;
        @JsonProperty("write-bytes-complete")
        private Boolean writeBytesComplete;

        @JsonProperty("read-records")
        private Long readRecords;
        @JsonProperty("read-records-complete")
        private Boolean readRecordsComplete;

        @JsonProperty("write-records")
        private Long writeRecords;
        @JsonProperty("write-records-complete")
        private Boolean writeRecordsComplete;
    }
}
