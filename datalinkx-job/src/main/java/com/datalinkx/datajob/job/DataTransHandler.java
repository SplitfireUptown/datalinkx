package com.datalinkx.datajob.job;

import java.util.Date;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.datajob.action.DataTransferAction;
import com.datalinkx.datajob.bean.DataTransJobParam;
import com.datalinkx.datajob.bean.JobExecCountDto;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.driver.utils.JobUtils;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Data Trans Job Handler
 */
@Component
public class DataTransHandler {
    private static Logger logger = LoggerFactory.getLogger(DataTransHandler.class);

    @Autowired
    private DataTransferAction dataTransferAction;

    @Autowired
    private DatalinkXServerClient dataServerClient;

    public DataTransJobDetail getJobDetail(DataTransJobParam jobParam) {
        return dataServerClient.getJobExecInfo(jobParam.getJobId()).getResult();
    }


    /**
     * data trans job
     */
    @XxlJob("dataTransJobHandler")
    public void dataTransJobHandler() throws InterruptedException {
        XxlJobHelper.log("begin dataTransJobHandler. ");
        DataTransJobParam jobParam = JsonUtils.toObject(XxlJobHelper.getJobParam(), DataTransJobParam.class);

        // 生成任务content
        JobUtils.initContext();
        // 定时异步调用无法统一trace_id，这里用job_id做trace_id
        MDC.put("trace_id", jobParam.getJobId());

        long startTime = new Date().getTime();
        DataTransJobDetail jobDetail;
        try {
            jobDetail = this.getJobDetail(jobParam);
            JobUtils.cntx().setTotal(jobDetail.getSyncUnits().size());
            dataTransferAction.doAction(jobDetail);
        } catch (InterruptedException e) {
            // cancel job
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            shutdownJob(startTime, jobParam, MetaConstants.JobStatus.JOB_STATUS_ERROR, e.getMessage());

            XxlJobHelper.handleFail(e.getMessage());
        }

        JobUtils.finiContext();

        XxlJobHelper.log("end dataTransJobHandler. ");
        // default success
        XxlJobHelper.handleSuccess("success");
    }

    private void shutdownJob(long startTime, DataTransJobParam jobParam, int status, String message) {
        JobExecCountDto jobExecCountDto = new JobExecCountDto();
        dataServerClient.updateJobStatus(JobStateForm.builder().jobId(jobParam.getJobId())
                .jobStatus(status).startTime(startTime).endTime(new Date().getTime())
                .errmsg(message).tbSuccess(JobUtils.cntx().getSuccess()).tbTotal(JobUtils.cntx().getTotal())
                .allCount(jobExecCountDto.getAllCount())
                .appendCount(jobExecCountDto.getAppendCount())
                .build());
    }
}
