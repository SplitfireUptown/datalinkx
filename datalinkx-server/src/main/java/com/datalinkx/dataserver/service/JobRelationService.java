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
 * @author: uptown
 * @date: 2024/4/25 22:50
 */
public interface JobRelationService {
    PageVo<List<JobVo.JobRelationVo>> page(JobForm.JobRelationPageForm jobRelationPageForm);
    void del(String relationId);
    String create(JobForm.JobRelationForm form);
    boolean checkJobHasCyclicDependency(Map<String, List<String>> taskGraph);
    JobVo.JobRelationBloodVo relationBloodInfo(String jobId);
}
