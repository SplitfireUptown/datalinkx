package com.datalinkx.dataserver.service;

import com.datalinkx.common.result.DatalinkXJobDetail;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.controller.form.JobStateForm;

/**
 * 任务流转运行时service
 */
public interface DtsJobService {
    /**
     * 获取任务JobGraph
     */
    DatalinkXJobDetail getJobExecInfo(String jobId);

    DatalinkXJobDetail getStreamJobExecInfo(String jobId);
    /**
     * 更新流转任务状态
     */
    String updateJobStatus(JobStateForm jobStateForm);
    /**
     * 更新任务关联任务
     */
    String updateJobTaskRel(String jobId, String taskId);
    /**
     * 更新任务同步模式&增量信息
     */
    void updateSyncMode(JobForm.SyncModifyForm syncMode);
    /**
     * 级联任务
     */
    String cascadeJob(String jobId);
    /**
     * 手动触发任务
     */
    void jobExec(String jobId);
}
