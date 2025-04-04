package com.datalinkx.dataserver.client;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.datalinkx.common.exception.DatalinkXSDKException;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataclient.client.xxljob.request.*;
import com.datalinkx.dataclient.client.xxljob.response.JobGroupPageListResp;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataclient.client.xxljob.XxlJobClient;
import com.datalinkx.dataclient.client.xxljob.response.ReturnT;
import com.datalinkx.dataserver.config.properties.XxlClientProperties;
import com.datalinkx.dataserver.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    public String trigger(String jobId, XxlJobParam jobParam) {
        return handleResult(client.trigger(getXxlJobId(jobId), JsonUtils.toJson(jobParam)));
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

    public String add(String cronExpr, XxlJobParam xxlJobParam) {
        JobGroupPageListResp jobGroupPageListResp = this.jobGroupPage(this.getDefaultJobGroupParam());
        Long jobGroupId = jobGroupPageListResp.getData().stream().findFirst().map(JobGroupPageListResp.JobGroupDetail::getId)
                .orElseThrow(() -> new DatalinkXSDKException("xxl-job job group not registered"));


        ReturnT<String> result = client.add(XxlJobInfo.builder()
                .jobDesc(xxlJobParam.getJobId())
                .author(GLOBAL_COMMON_GROUP)
                .scheduleType("CRON")
                .scheduleConf(cronExpr)
                .executorParam(JsonUtils.toJson(xxlJobParam))
                .executorBlockStrategy(xxlClientProperties.getExecutorBlockStrategy())
                .misfireStrategy(xxlClientProperties.getMisfireStrategy())
                .executorRouteStrategy(xxlClientProperties.getExecutorRouteStrategy())
                .glueType("BEAN")
                .jobGroup(jobGroupId)
                .executorFailRetryCount(0)
                .executorTimeout(0)
                .executorHandler(xxlClientProperties.getExecHandler())
                .build());

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
                .appname(GLOBAL_COMMON_GROUP)
                .title(GLOBAL_COMMON_GROUP)
                .start(0)
                .length(10)
                .build();
    }
}
