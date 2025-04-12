package com.datalinkx.rpc.client.datalinkxserver.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobSyncModeForm {
    private String jobId;
    private String increateValue;
}
