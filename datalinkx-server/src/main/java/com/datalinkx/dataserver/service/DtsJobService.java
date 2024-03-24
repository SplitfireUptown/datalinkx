package com.datalinkx.dataserver.service;

import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.controller.form.JobStateForm;
import com.datalinkx.driver.model.DataTransJobDetail;


public interface DtsJobService {
    DataTransJobDetail getJobExecInfo(String jobId);
    String updateJobStatus(JobStateForm jobStateForm);
    String updateJobTaskRel(String jobId, String taskId);
    void updateSyncMode(JobForm.SyncModifyForm syncMode);
    String cascadeJob(String jobId);
}
