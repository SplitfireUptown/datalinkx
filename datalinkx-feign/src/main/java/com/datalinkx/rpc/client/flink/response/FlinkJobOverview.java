package com.datalinkx.rpc.client.flink.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlinkJobOverview {
    private String jid;
    private String name;
    private String state;
}
