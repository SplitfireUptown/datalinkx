package com.datalinkx.dataserver.service;

import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.JobForm;

import java.util.List;

/**
 * 流式任务service
 */
public interface StreamJobService {

    /**
     * 创建流式任务
     */
    String createStreamJob(JobForm.JobCreateForm form);

    /**
     * 编辑流式任务
     */
    String modifyStreamJob(JobForm.JobModifyForm form);

    /**
     * 更新任务状态
     */
    void startStreamJob(String jobId);

    /**
     * 任务分页查询
     */
    PageVo<List<JobVo.JobStreamPageVo>> streamPage(JobForm.JobPageForm form);

    /**
     * 执行流式任务
     */
    void streamJobExec(String jobId, String lockId);

    /**
     * 停止流式任务
     */
    void stop(String jobId);

    /**
     * 暂停流式任务，只停止flink任务，不修改datalinkx任务状态
     */
    void pause(String jobId);

    /**
     * 删除流式任务
     */
    void delete(String jobId);
}
