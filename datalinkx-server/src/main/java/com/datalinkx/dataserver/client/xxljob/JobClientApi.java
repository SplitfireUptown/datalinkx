package com.datalinkx.dataserver.client.xxljob;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.datalinkx.common.result.StatusCode;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.domain.JobBean;
import com.datalinkx.dataserver.client.xxljob.request.DataTransJobParam;
import com.datalinkx.dataserver.client.xxljob.request.LogQueryParam;
import com.datalinkx.dataserver.client.xxljob.request.PageQueryParam;
import com.datalinkx.dataserver.client.xxljob.request.XxlJobInfo;
import com.datalinkx.dataserver.client.xxljob.response.ReturnT;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.dataserver.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



/**
 * @author uptown
 * @Description TODO
 * @createTime 2021年05月21日 18:11:49
 */
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

    
    public String start(String jobId) {
        return handleResult(client.start(getXxlJobId(jobId)));
    }

    public String stop(String jobId) {
        return handleResult(client.stop(getXxlJobId(jobId)));
    }

    public String trigger(String jobId, DataTransJobParam jobParam) {
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
        return jobRepository.findByJobId(jobId).isPresent();
    }

    public String add(String jobId, String cronExpr, DataTransJobParam dataTransJobParam) {
        ReturnT<String> result = client.add(XxlJobInfo.builder()
                .jobDesc(jobId)
                .author("datalinkX")
                .scheduleType("CRON")
                .scheduleConf(cronExpr)
                .executorParam(JsonUtils.toJson(dataTransJobParam))
                .executorBlockStrategy("SERIAL_EXECUTION")
                .misfireStrategy("DO_NOTHING")
                .executorRouteStrategy("FIRST")
                .glueType("BEAN")
                .jobGroup(jobGroup)
                .executorFailRetryCount(0)
                .executorTimeout(0)
                .executorHandler(execHandler)
                .build());

        return handleResult(result);
    }

    public String remove(String jobId) {
        String res = handleResult(client.remove(getXxlJobId(jobId)));
        jobRepository.logicDeleteByJobId(jobId);
        return res;
    }

    public boolean isJobRunning(String jobId) {
        XxlJobInfo xxlJobInfo = getJobInfo(jobId);
        Map<String, Object> logs = client.logList(LogQueryParam.builder()
                .start(0).length(PAGE_SIZE).jobGroup(jobGroup).jobId(xxlJobInfo.getId()).logStatus(LOG_STATUS_RUNNING).build());

        return logs.size() > 0;
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
        if (StringUtils.isEmpty(jobBean.getXxlId())) {
            throw new DatalinkXServerException(StatusCode.INVALID_ARGUMENTS, "invalid xxl job id");
        }

        return Integer.parseInt(jobBean.getXxlId());
    }
}
