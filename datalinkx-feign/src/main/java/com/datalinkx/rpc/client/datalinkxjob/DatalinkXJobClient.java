package com.datalinkx.rpc.client.datalinkxjob;

import com.datalinkx.common.result.WebResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "datalinkxjob", url = "${client.datalinkxjob.url}")
@ConditionalOnProperty(prefix = "client.datalinkxjob", name = "url")
public interface DatalinkXJobClient {
    @PostMapping("/data/transfer/stream_exec")
    WebResult<String> dataTransExec(@RequestParam("detail") String datalinkXJobDetail);

    @PostMapping("/data/transfer/job_health")
    WebResult<String> jobHealth(@RequestParam("jobId") String jobId, @RequestParam("type") Integer type);
}
