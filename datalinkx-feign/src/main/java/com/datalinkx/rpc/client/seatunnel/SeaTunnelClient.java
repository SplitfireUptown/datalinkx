package com.datalinkx.rpc.client.seatunnel;

import com.datalinkx.rpc.client.seatunnel.request.ComputeJobGraph;
import com.datalinkx.rpc.client.seatunnel.response.JobCommitResp;
import com.datalinkx.rpc.client.seatunnel.response.JobOverviewResp;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "seaTunnelClient", url = "${client.seatunnel.url}")
@ConditionalOnProperty(prefix = "client.seatunnel", name = "url")
public interface SeaTunnelClient {

    @PostMapping("/hazelcast/rest/maps/submit-job")
    JobCommitResp jobSubmit(@RequestBody ComputeJobGraph computeJobGraph);

    @GetMapping("/hazelcast/rest/maps/job-info/{jobId}")
    JobOverviewResp jobOverview(@PathVariable("jobId") String jobId);
}
