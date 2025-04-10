package com.datalinkx.dataserver.controller;


import com.datalinkx.common.result.DatalinkXJobDetail;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.controller.form.JobStateForm;
import com.datalinkx.dataserver.service.DtsJobService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/job_graph")
public class JobGraphController {

    @Autowired
    private DtsJobService dtsJobService;

    @ApiOperation("获取流转任务job_graph")
    @RequestMapping("/execute_info")
    public WebResult<DatalinkXJobDetail> getJobExecInfo(String jobId) {
        return WebResult.of(dtsJobService.getJobExecInfo(jobId));
    }

    @ApiOperation("级联触发流转任务")
    @RequestMapping("/cascade_job")
    public WebResult<String> cascadeJob(String jobId) {
        return WebResult.of(this.dtsJobService.cascadeJob(jobId));
    }

    @ApiOperation("更新流转任务状态")
    @RequestMapping("/update_job")
    public WebResult<String> updateJobStatus(JobStateForm jobStateForm) {
        return WebResult.of(this.dtsJobService.updateJobStatus(jobStateForm));
    }

    @ApiOperation("更新流转任务增量条件")
    @RequestMapping("/update_sync_mode")
    public WebResult<String> updateSyncMode(JobForm.SyncModifyForm syncmodifyForm) {
        this.dtsJobService.updateSyncMode(syncmodifyForm);
        return WebResult.of(syncmodifyForm.getJobId());
    }


    @ApiOperation("绑定业务JOB id与执行任务task_id关联")
    @PostMapping("/update_job_task_rel")
    public WebResult<String> updateJobTaskRel(String jobId, String taskId) {
        return WebResult.of(this.dtsJobService.updateJobTaskRel(jobId, taskId));
    }
}
