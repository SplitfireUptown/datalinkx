package com.datalinkx.datajob.job;

import java.util.Date;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.datajob.action.DataTransferAction;
import com.datalinkx.datajob.action.StreamDataTransferAction;
import com.datalinkx.datajob.bean.JobExecCountDto;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.bean.XxlJobParam;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * Data Trans Job Handler
 */
@RestController
@RequestMapping("/data/transfer")
public class  DataTransHandler {
    private static Logger logger = LoggerFactory.getLogger(DataTransHandler.class);

    @Autowired
    private DataTransferAction dataTransferAction;

    @Autowired
    private StreamDataTransferAction streamDataTransferAction;

    @Autowired
    private DatalinkXServerClient dataServerClient;

    public DataTransJobDetail getJobDetail(String jobId) {
        return dataServerClient.getJobExecInfo(jobId).getResult();
    }

    @SneakyThrows
    @RequestMapping("/stream_exec")
    public String streamJobHandler(String detail) {
        DataTransJobDetail dataTransJobDetail = JsonUtils.toObject(detail, DataTransJobDetail.class);
        streamDataTransferAction.doAction(dataTransJobDetail);
        return dataTransJobDetail.getJobId();
    }

    /**
     * data trans job
     */
    @XxlJob("dataTransJobHandler")
    public void dataTransJobHandler() throws InterruptedException {
        XxlJobHelper.log("begin dataTransJobHandler. ");
        XxlJobParam jobParam = JsonUtils.toObject(XxlJobHelper.getJobParam(), XxlJobParam.class);
        String jobId = jobParam.getJobId();

        // 定时异步调用无法统一trace_id，这里用job_id做trace_id
        MDC.put("trace_id", jobId);

        long startTime = new Date().getTime();
        DataTransJobDetail jobDetail;
        try {
            jobDetail = this.getJobDetail(jobId);
            dataTransferAction.doAction(jobDetail);
        } catch (InterruptedException e) {
            // cancel job
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            this.shutdownJob(startTime, jobId, e.getMessage());

            XxlJobHelper.handleFail(e.getMessage());
        }

        XxlJobHelper.log("end dataTransJobHandler. ");
        // default success
        XxlJobHelper.handleSuccess("success");
    }

    private void shutdownJob(long startTime, String jobId, String message) {
        JobExecCountDto jobExecCountDto = new JobExecCountDto();
        dataServerClient.updateJobStatus(JobStateForm.builder().jobId(jobId)
                .jobStatus(MetaConstants.JobStatus.JOB_STATUS_ERROR).startTime(startTime).endTime(new Date().getTime())
                .errmsg(message).allCount(jobExecCountDto.getAllCount())
                .appendCount(jobExecCountDto.getAppendCount())
                .build());
    }
}
