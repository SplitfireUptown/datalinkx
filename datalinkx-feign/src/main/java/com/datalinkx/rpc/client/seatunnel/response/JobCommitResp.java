package com.datalinkx.rpc.client.seatunnel.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: uptown
 * @date: 2024/11/22 23:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JobCommitResp {
    private String jobId;
    private String jobName;
}
