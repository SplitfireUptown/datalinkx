package com.datalinkx.dataserver.service;

import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.JobForm;

import java.util.List;

/**
 * @author: uptown
 * @date: 2024/4/25 22:44
 */
public interface JobService {
    public String jobCreateProxy(JobForm.JobCreateForm form);
    String jobModify(JobForm.JobModifyForm form);
    JobVo.JobInfoVo info(String jobId);
    PageVo<List<JobVo.JobPageVo>> page(JobForm.JobPageForm form);
    void del(String jobId);
    PageVo<List<JobVo.JobLogPageVo>> logPage(JobForm.JobLogPageForm jobLogPageForm);
    void jobStop(String jobId);
    List<JobVo.JobId2NameVo> list();
}
