package com.datalinkx.dataserver.client;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.config.properties.XxlClientProperties;
import com.datalinkx.dataserver.repository.JobRepository;
import com.datalinkx.rpc.client.xxljob.XxlJobClient;
import com.datalinkx.rpc.client.xxljob.request.PageQueryParam;
import com.datalinkx.rpc.client.xxljob.request.XxlJobGroupParam;
import com.datalinkx.rpc.client.xxljob.request.XxlJobInfo;
import com.datalinkx.rpc.client.xxljob.response.JobGroupPageListResp;
import com.datalinkx.rpc.client.xxljob.response.ReturnT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.datalinkx.common.constants.MessageHubConstants.GLOBAL_COMMON_GROUP;


@Component
@Slf4j
public class JobClientApi {
    private static final int SUCCESS_CODE = 200;

    @Autowired
    XxlJobClient client;

    @Autowired
    JobRepository jobRepository;

    @Autowired
    XxlClientProperties xxlClientProperties;

    
    public String start(String jobId) {
        return handleResult(client.start(getXxlJobId(jobId)));
    }

    public String stop(String jobId) {
        return handleResult(client.stop(getXxlJobId(jobId)));
    }

    public String del(String jobId) {
        return handleResult(client.remove(getXxlJobId(jobId)));
    }

    public String trigger(String jobId) {
        return handleResult(client.trigger(getXxlJobId(jobId), jobId));
    }


    public JobGroupPageListResp jobGroupPage(PageQueryParam pageQueryParam) {
        return client.jobGroupPage(pageQueryParam);
    }

    public String jobGroupSave(XxlJobGroupParam xxlJobGroupParam) {
        return handleResult(client.jobGroupSave(xxlJobGroupParam));
    }

    public boolean isXxljobExist(String jobId) {
        JobBean jobNotExist = jobRepository.findByJobId(jobId).orElseThrow(
                () -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "datalinkx job not exist"));
        return !ObjectUtils.isEmpty(jobNotExist.getXxlId());
    }

    public String add(String cronExpr, String jobId) {
        JobGroupPageListResp jobGroupPageListResp = this.jobGroupPage(this.getDefaultJobGroupParam());
        Long jobGroupId = jobGroupPageListResp.getData().stream().findFirst().map(JobGroupPageListResp.JobGroupDetail::getId)
                .orElseThrow(() -> new DatalinkXServerException("xxl-job job group not registered"));

        XxlJobInfo xxlJobInfo = XxlJobInfo.builder()
                .jobDesc(jobId)
                .author(GLOBAL_COMMON_GROUP)
                .executorParam(jobId)
                .executorBlockStrategy(xxlClientProperties.getExecutorBlockStrategy())
                .misfireStrategy(xxlClientProperties.getMisfireStrategy())
                .executorRouteStrategy(xxlClientProperties.getExecutorRouteStrategy())
                .glueType("BEAN")
                .jobGroup(jobGroupId)
                .executorFailRetryCount(0)
                .executorTimeout(0)
                .executorHandler(xxlClientProperties.getExecHandler())
                .build();

        if (!ObjectUtils.isEmpty(cronExpr)) {
            xxlJobInfo.setScheduleType(MetaConstants.JobType.JOB_CRON_SCHEDULE_TYPE);
            xxlJobInfo.setScheduleConf(cronExpr);
        } else {
            xxlJobInfo.setScheduleType(MetaConstants.JobType.JOB_NONE_SCHEDULE_TYPE);
            xxlJobInfo.setScheduleConf(MetaConstants.JobType.JOB_RATE_SCHEDULE_CONF);
        }

        ReturnT<String> result = client.add(xxlJobInfo);

        return handleResult(result);
    }



    private String handleResult(ReturnT<String> returnT) {
        if (returnT.getCode() == SUCCESS_CODE) {
            return returnT.getContent();
        } else {
            log.error(returnT.getMsg());
            throw new DatalinkXServerException(returnT.getMsg());
        }
    }


    public Integer getXxlJobId(String jobId) {
        JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "xxljob not exist"));
        return Integer.parseInt(jobBean.getXxlId());
    }

    public PageQueryParam getDefaultJobGroupParam() {
        return PageQueryParam
                .builder()
                .appname(xxlClientProperties.getExecHandler())
                .title(xxlClientProperties.getExecHandler())
                .start(0)
                .length(10)
                .build();
    }
}
