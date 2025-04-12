package com.datalinkx.rpc.client.flink.request;

import lombok.Data;

@Data
public class FlinkJobStopReq {
    private String targetDirectory;
    private Boolean drain;
}
