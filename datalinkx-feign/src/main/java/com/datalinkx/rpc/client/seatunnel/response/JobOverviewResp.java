package com.datalinkx.rpc.client.seatunnel.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author: uptown
 * @date: 2024/11/22 23:05
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobOverviewResp {
    private String jobId;
    private String jobName;
    private String jobStatus;
    private String errorMsg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Timestamp finishTime;
    private Metrics metrics;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metrics {
        private String SourceReceivedCount;
        private String SourceReceivedQPS;
        private String SourceReceivedBytes;
        private String SourceReceivedBytesPerSeconds;
        private String SinkWriteCount;
        private String SinkWriteQPS;
        private String SinkWriteBytes;
        private String SinkWriteBytesPerSeconds;
    }
}
