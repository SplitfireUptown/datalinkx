package com.datalinkx.rpc.client.seatunnel.request;

import lombok.Data;

@Data
public class JobStopReq {
    private String jobId;
    private Boolean isStopWithSavePoint;
}
