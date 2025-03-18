package com.datalinkx.dataserver.client.xxljob;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.client.xxljob.request.XxlJobParam;
import com.datalinkx.dataserver.client.xxljob.request.LogQueryParam;
import com.datalinkx.dataserver.client.xxljob.request.PageQueryParam;
import com.datalinkx.dataserver.client.xxljob.request.XxlJobInfo;
import com.datalinkx.dataserver.client.xxljob.request.XxlJobParam;
import com.datalinkx.dataserver.client.xxljob.response.ReturnT;
import com.datalinkx.dataserver.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;




@Component
@Slf4j
public class JobClientApi {
    private static final String DATA_FIELD = "data";
    private static final int SUCCESS_CODE = 200;
    private static final int PAGE_SIZE = 20000;
    private static final int LOG_STATUS_RUNNING = 3;

    @Autowired
    XxlJobClient client;

    @Autowired
    JobRepository jobRepository;

    @Value("${xxl-job.job-group}")
    Integer jobGroup;

    @Value("${xxl-job.exec-handler}")
    String execHandler;

    // 分布式执行器需要配置 - FAILOVER：故障转移策略
    @Value("${xxl-job.executor-route-strategy:FIRST}")
    String executorRouteStrategy;

    
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
//        XxlJobInfo xxlJobInfo = getJobInfo(jobId);
        return handleResult(client.trigger(getXxlJobId(jobId), JsonUtils.toJson(jobParam)));
    }

    public int cancel(String jobId) {
        if (!isXxljobExist(jobId)) {
            return 0;
        }

        XxlJobInfo xxlJobInfo = getJobInfo(jobId);
        Map<String, Object> logs = client.logList(LogQueryParam.builder()
                .start(0).length(PAGE_SIZE).jobGroup(jobGroup).jobId(xxlJobInfo.getId()).logStatus(LOG_STATUS_RUNNING).build());
        ((List<Map<String, Object>>) logs.get(DATA_FIELD))
                .forEach(log -> client.logKill(Integer.parseInt(String.valueOf(log.get("id")))));
        return ((List<Map<String, Object>>) logs.get(DATA_FIELD)).size();
    }

    public boolean isXxljobExist(String jobId) {
        JobBean jobNotExist = jobRepository.findByJobId(jobId).orElseThrow(
                () -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "job not exist"));
        return !ObjectUtils.isEmpty(jobNotExist.getXxlId());
    }

    public String add(String cronExpr, XxlJobParam xxlJobParam) {
        ReturnT<String> result = client.add(XxlJobInfo.builder()
                .jobDesc(xxlJobParam.getJobId())
                .author("datalinkX")
                .scheduleType("CRON")
                .scheduleConf(cronExpr)
                .executorParam(JsonUtils.toJson(xxlJobParam))
                .executorBlockStrategy("SERIAL_EXECUTION")
                .misfireStrategy("DO_NOTHING")
                .executorRouteStrategy(executorRouteStrategy)
                .glueType("BEAN")
                .jobGroup(jobGroup)
                .executorFailRetryCount(0)
                .executorTimeout(0)
                .executorHandler(execHandler)
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

    private XxlJobInfo getJobInfo(String jobId) {
        int xxlJobId = getXxlJobId(jobId);

        Map<String, Object> jobMap = client.pageList(PageQueryParam.builder()
                .start(0)
                .length(PAGE_SIZE)
                .jobGroup(jobGroup)
                .jobDesc(jobId)  //databridge上的jobId对应xxljob上的jobDesc
                .triggerStatus(-1)
                .build());

        List<XxlJobInfo> matchJobList =
                ((List<Map<String, Object>>) jobMap.get(DATA_FIELD)).stream().filter(job -> job.get("id").equals(xxlJobId))
                        .map(x -> JsonUtils.toObject(JsonUtils.map2Json(x), XxlJobInfo.class)).collect(Collectors.toList());

        if (matchJobList.isEmpty()) {
            throw new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "xxljob not exist");
        }

        return matchJobList.get(0);
    }

    public Integer getXxlJobId(String jobId) {
        JobBean jobBean = jobRepository.findByJobId(jobId).orElseThrow(() -> new DatalinkXServerException(StatusCode.JOB_NOT_EXISTS, "xxljob not exist"));
        return Integer.parseInt(jobBean.getXxlId());
    }
}
