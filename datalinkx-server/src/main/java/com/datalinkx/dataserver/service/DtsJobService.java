package com.datalinkx.dataserver.service;

import java.util.List;

import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.controller.form.JobStateForm;
import com.datalinkx.driver.model.DataTransJobDetail;


public interface DtsJobService {
    DataTransJobDetail getJobExecInfo(String jobId, List<String> tableIds, Boolean tbDetail);
    String updateJobStatus(JobStateForm jobStateForm);
    String updateJobTaskRel(String jobId, String taskId);
    void updateSyncMode(JobForm.SyncModifyForm syncMode);
}
