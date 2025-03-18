package com.datalinkx.dataserver.service.impl;

import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_STOP;
import static com.datalinkx.common.utils.IdUtils.genKey;
import static com.datalinkx.common.utils.JsonUtils.toJson;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataclient.client.datalinkxjob.DatalinkXJobClient;
import com.datalinkx.dataclient.client.flink.FlinkClient;
import com.datalinkx.dataclient.client.flink.request.FlinkJobStopReq;
import com.datalinkx.dataclient.client.flink.response.FlinkJobOverview;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.client.xxljob.JobClientApi;
import com.datalinkx.dataserver.config.CommonProperties;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.repository.DsRepository;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.dataserver.service.DtsJobService;
import com.datalinkx.dataserver.service.StreamJobService;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
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

    @Autowired
    JobClientApi jobClientApi;

    @Autowired
    FlinkClient flinkClient;

    @Autowired
    CommonProperties commonProperties;


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
        jobBean.setSyncMode(JsonUtils.toJson(form.getSyncMode()));
        jobRepository.save(jobBean);
        return form.getJobId();
    }

    @Override
    public void startStreamJob(String jobId) {
        JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
        if (MetaConstants.JobStatus.JOB_STATUS_SYNCING == jobBean.getStatus()) {
            throw new DatalinkXServerException(StatusCode.JOB_IS_RUNNING, "任务运行中");
        }
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

    @Async
    @Override
    public void streamJobExec(String jobId, String lockId) {
        DataTransJobDetail jobExecInfo = dtsJobService.getStreamJobExecInfo(jobId);
        jobExecInfo.setLockId(lockId);
        if (!ObjectUtils.isEmpty(jobExecInfo.getSyncUnit().getCheckpoint())) {
            String checkpoint = jobExecInfo.getSyncUnit().getCheckpoint().replace("file://", "");
            // 如果之前记录的checkpoint目录存在，则删除
            File directory = new File(checkpoint);
            if (directory.exists()) {
                File[] files = directory.listFiles();
                if (!ObjectUtils.isEmpty(files)) {
                    jobExecInfo.getSyncUnit().setCheckpoint(files[0].getPath());
                }
            } else {
                jobExecInfo.getSyncUnit().setCheckpoint("");
            }
        }
        datalinkXJobClient.dataTransExec(JsonUtils.toJson(jobExecInfo));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void stop(String jobId) {
        JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
        if (JOB_STATUS_STOP == jobBean.getStatus()) {
            throw new DatalinkXServerException(StatusCode.JOB_IS_STOP, "任务已停止");
        }

        // 记录checkpoint
        this.stopFlinkTask(jobBean);

        jobBean.setStatus(JOB_STATUS_STOP);
        jobRepository.save(jobBean);
    }

    /**
     * 停止flink任务
     */
    private void stopFlinkTask(JobBean jobBean) {
        // 记录checkpoint
        if (!ObjectUtils.isEmpty(jobBean.getTaskId())) {
            FlinkJobStopReq flinkJobStopReq = new FlinkJobStopReq();
            flinkJobStopReq.setDrain(true);
            String checkpoint = String.format("%s/%s", commonProperties.getCheckpointPath(), jobBean.getJobId());

            // 如果之前记录的checkpoint目录存在，则删除
            File directory = new File(checkpoint);
            if (!directory.exists()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }

            boolean isRunning = true;
            while (isRunning) {
                flinkJobStopReq.setTargetDirectory(checkpoint);
                flinkClient.jobStop(jobBean.getTaskId(), flinkJobStopReq);


                // 检查任务是否已经停止
                JsonNode jsonNode = flinkClient.jobOverview();
                Map<String, FlinkJobOverview> jobId2TaskInfo = JsonUtils.toList(JsonUtils.toJson(jsonNode.get("jobs")), FlinkJobOverview.class)
                        .stream()
                        .filter(task -> "RUNNING".equalsIgnoreCase(task.getState()))
                        .collect(Collectors.toMap(FlinkJobOverview::getName, v -> v));

                if (jobId2TaskInfo.containsKey(jobBean.getJobId())) {

                    FlinkJobOverview flinkJobOverview = jobId2TaskInfo.remove(jobBean.getJobId());
                    flinkClient.jobStop(flinkJobOverview.getJid(), flinkJobStopReq);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    isRunning = false;
                }
            }
            jobBean.setCheckpoint(checkpoint);
        }
    }

    @Override
    public void pause(String jobId) {
        JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
        if (!ObjectUtils.isEmpty(jobBean.getTaskId())) {
            this.stopFlinkTask(jobBean);
            // 记录checkpoint
            jobRepository.save(jobBean);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(String jobId) {
        this.stop(jobId);
        jobService.del(jobId);
    }
}
