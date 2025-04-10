package com.datalinkx.dataserver.service;

import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.JobForm;

import java.util.List;

/**
 * 业务流转任务service
 */
public interface JobService {
    /**
     * 创建任务
     */
    String jobCreateProxy(JobForm.JobCreateForm form);
    /**
     * 编辑任务
     */
    String jobModify(JobForm.JobModifyForm form);
    /**
     * 任务详情
     */
    JobVo.JobInfoVo info(String jobId);
    /**
     * 任务分页查询
     */
    PageVo<List<JobVo.JobPageVo>> page(JobForm.JobPageForm form);
    /**
     * 删除任务
     */
    void del(String jobId, Boolean stream);
    /**
     * 任务日志分页查询
     */
    PageVo<List<JobVo.JobLogPageVo>> logPage(JobForm.JobLogPageForm jobLogPageForm);
    /**
     * 任务停止
     */
    void jobStop(String jobId);
    /**
     * 任务列表查询
     */
    List<JobVo.JobId2NameVo> list();
}
