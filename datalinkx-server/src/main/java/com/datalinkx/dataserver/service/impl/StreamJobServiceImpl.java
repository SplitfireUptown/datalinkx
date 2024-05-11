package com.datalinkx.dataserver.service.impl;

import static com.datalinkx.common.utils.IdUtils.genKey;
import static com.datalinkx.common.utils.JsonUtils.toJson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataclient.client.datalinkxjob.DatalinkXJobClient;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.repository.DsRepository;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.DtsJobService;
import com.datalinkx.dataserver.service.StreamJobService;
import com.datalinkx.driver.model.DataTransJobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StreamJobServiceImpl implements StreamJobService {

    @Autowired
    JobRepository jobRepository;

    @Autowired
    JobServiceImpl jobService;

    @Autowired
    DsRepository dsRepository;

    @Autowired
    DatalinkXJobClient datalinkXJobClient;

    @Autowired
    DtsJobService dtsJobService;

    @Override
    public String createStreamJob(JobForm.JobCreateForm form) {
        form.setType(MetaConstants.JobType.JOB_TYPE_STREAM);
        jobService.validJobForm(form);
        JobBean jobBean = new JobBean();
        String jobId = genKey("job");
        jobBean.setJobId(jobId);

        jobBean.setType(MetaConstants.JobType.JOB_TYPE_STREAM);
        jobBean.setReaderDsId(form.getFromDsId());
        jobBean.setWriterDsId(form.getToDsId());

        jobBean.setConfig(toJson(form.getFieldMappings()));
        jobBean.setFromTbId(form.getFromTbName());
        jobBean.setToTbId(form.getToTbName());
        jobBean.setStatus(MetaConstants.JobStatus.JOB_STATUS_CREATE);
        jobBean.setName(form.getJobName());

        jobRepository.save(jobBean);
        return jobId;
    }

    @Override
    public String modifyStreamJob(JobForm.JobModifyForm form) {
        jobService.validJobForm(form);
        JobBean jobBean = jobRepository.findByJobId(form.getJobId()).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
        jobBean.setReaderDsId(form.getFromDsId());
        jobBean.setWriterDsId(form.getToDsId());
        jobBean.setConfig(toJson(form.getFieldMappings()));
        jobBean.setFromTbId(form.getFromTbName());
        jobBean.setToTbId(form.getToTbName());
        jobBean.setName(form.getJobName());
        jobRepository.save(jobBean);
        return form.getJobId();
    }

    @Override
    public void startStreamJob(String jobId) {
        jobRepository.updateJobStatus(jobId, MetaConstants.JobStatus.JOB_STATUS_SYNCING);
    }

    @Override
    public PageVo<List<JobVo.JobStreamPageVo>> streamPage(JobForm.JobPageForm form) {
        PageRequest pageRequest = PageRequest.of(form.getPageNo() - 1, form.getPageSize());
        Page<JobBean> jobBeans = jobRepository.pageQuery(pageRequest, form.getType());


        List<String> dsId = new ArrayList<>();
        jobBeans.getContent().forEach(job -> {
            dsId.add(job.getWriterDsId());
            dsId.add(job.getReaderDsId());
        });


        Map<String, String> dsNameMap = dsRepository.findAllByDsIdIn(dsId)
                .stream()
                .collect(Collectors.toMap(DsBean::getDsId, DsBean::getName));

        List<JobVo.JobStreamPageVo> pageVoList = jobBeans.getContent().stream().map(jobBean -> JobVo.JobStreamPageVo
                .builder()
                .jobId(jobBean.getJobId())
                .jobName(jobBean.getName())
                .fromTbName(dsNameMap.get(jobBean.getReaderDsId()) + "." + jobBean.getFromTbId())
                .toTbName(dsNameMap.get(jobBean.getWriterDsId()) + "."  + jobBean.getToTbId())
                .startTime(jobBean.getStartTime())
                .build()).collect(Collectors.toList());

        PageVo<List<JobVo.JobStreamPageVo>> result = new PageVo<>();
        result.setPageNo(form.getPageNo());
        result.setPageSize(form.getPageSize());
        result.setData(pageVoList);
        result.setTotalPage(jobBeans.getTotalPages());
        result.setTotal(jobBeans.getTotalElements());
        return result;
    }

    @Override
    public void streamJobExec(String jobId) {
        DataTransJobDetail jobExecInfo = dtsJobService.getStreamJobExecInfo(jobId);
        datalinkXJobClient.dataTransExec(JsonUtils.toJson(jobExecInfo));
    }

    @Override
    public void stop(String jobId) {

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(String jobId) {
        this.stop(jobId);
        jobService.del(jobId);
    }
}
