package com.datalinkx.rpc.client.datalinkxserver;

import com.datalinkx.common.result.DatalinkXJobDetail;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.rpc.client.datalinkxserver.request.JobStateForm;
import com.datalinkx.rpc.client.datalinkxserver.request.JobSyncModeForm;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "datalinkxserver", url = "${client.datalinkxserver.url}")
@ConditionalOnProperty(prefix = "client.datalinkxserver", name = "url")
public interface DatalinkXServerClient {

    @GetMapping("/api/job_graph/execute_info")
    WebResult<DatalinkXJobDetail> getJobExecInfo(@RequestParam("jobId") String jobId);

    @PostMapping("/api/job_graph/cascade_job")
    WebResult<String> cascadeJob(@RequestParam("jobId") String jobId);

    @PostMapping("/api/job_graph/update_job")
    WebResult<String> updateJobStatus(@SpringQueryMap JobStateForm jobStateForm);

    @PostMapping("/api/job_graph/update_sync_mode")
    WebResult<String> updateSyncMode(@SpringQueryMap JobSyncModeForm jobSyncModeForm);

    @PostMapping("/api/job_graph/update_job_task_rel")
    WebResult<String> updateJobTaskRel(@RequestParam("jobId") String jobId, @RequestParam("taskId") String taskId);

    @PostMapping("/api/mcp/job/delete_by_name")
    WebResult<String> deleteJobByName(@RequestParam("name") String jobName);

    @PostMapping("/api/mcp/job/trigger_by_name")
    WebResult<String> triggerJobByName(@RequestParam("name") String jobName);
}
