package com.datalinkx.dataserver.service;

import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.domain.JobRelationBean;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.JobForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.datalinkx.common.utils.IdUtils.genKey;

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
}
