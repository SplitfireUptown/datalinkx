package com.datalinkx.dataserver.service;

import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.JobForm;

import java.util.List;

/**
 * 流转任务血缘级联配置service
 */
public interface JobRelationService {
    /**
     * 分页查询
     */
    PageVo<List<JobVo.JobRelationVo>> page(JobForm.JobRelationPageForm jobRelationPageForm);

    /**
     * 删除
     */
    void del(String relationId);

    /**
     * 创建
     */
    String create(JobForm.JobRelationForm form);

    /**
     * 任务血缘信息
     */
    JobVo.JobRelationBloodVo relationBloodInfo(String jobId);

    /**
     *  删除指定任务下的血缘关系，无论父子关系
     */
    void delJobRelation(String jobId);
}
