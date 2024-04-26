package com.datalinkx.dataserver.service;

import java.util.List;

import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.JobForm;

/**
 * 流式任务service
 */
public interface StreamJobService {

    /**
     * 创建流式任务
     */
    String createStreamJob(JobForm.JobCreateForm form);

    /**
     * 任务分页查询
     */
    PageVo<List<JobVo.JobStreamPageVo>> streamPage(JobForm.JobPageForm form);

    /**
     * 执行流式任务
     */
    void streamJobExec(String jobId);
}
