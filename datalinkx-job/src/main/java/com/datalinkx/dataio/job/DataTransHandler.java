package com.datalinkx.dataio.job;

import java.util.Date;

import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.driver.utils.JobUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataio.action.FlinkAction;
import com.datalinkx.dataio.bean.DataTransJobParam;
import com.datalinkx.dataio.bean.JobExecCountDto;
import com.datalinkx.dataio.bean.JobStateForm;
import com.datalinkx.dataio.client.datalinkxserver.DatalinkXServerClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


/**
 * Data Trans Job Handler
 */
@Component
public class DataTransHandler {
    private static Logger logger = LoggerFactory.getLogger(DataTransHandler.class);

    @Autowired
    private FlinkAction flinkAction;

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

        // 生成任务ID
        JobUtils.initContext();

        long startTime = new Date().getTime();
        DataTransJobDetail jobDetail;
        try {
            jobDetail = this.getJobDetail(jobParam);
            JobUtils.cntx().setTotal(jobDetail.getSyncUnits().size());
            flinkAction.doAction(jobDetail);
        } catch (InterruptedException e) {
            // cancel job
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            shutdownJob(startTime, jobParam, 1, e.getMessage());

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
