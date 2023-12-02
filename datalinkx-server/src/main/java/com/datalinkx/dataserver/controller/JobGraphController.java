package com.datalinkx.dataserver.controller;


import java.util.List;

import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.controller.form.JobStateForm;
import com.datalinkx.dataserver.service.DtsJobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/job_graph")
public class JobGraphController {

    @Autowired
    private DtsJobService dtsJobService;

    @RequestMapping("/execute_info")
    public WebResult<DataTransJobDetail> getJobExecInfo(String jobId,
                                             @RequestParam(value = "tableIds", required = false) List<String> tableIds,
                                             @RequestParam(value = "tbDetail", defaultValue = "false") Boolean tbDetail) {
        return WebResult.of(dtsJobService.getJobExecInfo(jobId, tableIds, tbDetail));
    }

    @RequestMapping("/update_job")
    public WebResult<String> updateJobStatus(JobStateForm jobStateForm) {
        return WebResult.of(this.dtsJobService.updateJobStatus(jobStateForm));
    }

    @RequestMapping("/update_sync_mode")
    public WebResult<String> updateSyncMode(JobForm.SyncModifyForm syncmodifyForm) {
        this.dtsJobService.updateSyncMode(syncmodifyForm);
        return WebResult.of(syncmodifyForm.getJobId());
    }


    @PostMapping("/update_job_task_rel")
    public WebResult<String> updateJobTaskRel(String jobId, String taskId) {
        return WebResult.of(dtsJobService.updateJobTaskRel(jobId, taskId));
    }
}
