package com.datalinkx.datajob.job;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.datalinkx.common.constants.MessageHubConstants;
import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.common.utils.IdUtils;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.datajob.action.AbstractDataTransferAction;
import com.datalinkx.datajob.action.DataTransferAction;
import com.datalinkx.datajob.action.StreamDataTransferAction;
import com.datalinkx.datajob.action.TransformDataTransferAction;
import com.datalinkx.datajob.bean.JobExecCountDto;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.bean.XxlJobParam;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.driver.model.DataTransJobDetail;
import com.datalinkx.messagehub.service.redis.RedisPubSubProcessor;
import com.datalinkx.messagehub.service.redis.RedisQueueProcessor;
import com.datalinkx.messagehub.service.redis.RedisStreamProcessor;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;


/**
 * Data Trans Job Handler
 */
@RestController
@RequestMapping("/data/transfer")
public class DataTransHandler {
    private static Logger logger = LoggerFactory.getLogger(DataTransHandler.class);

    @Autowired
    private DataTransferAction dataTransferAction;

    @Autowired(required = false)
    private TransformDataTransferAction transformDataTransferAction;

    @Autowired
    private StreamDataTransferAction streamDataTransferAction;

    @Autowired
    private DatalinkXServerClient dataServerClient;

    public Map<Integer, AbstractDataTransferAction> actionEngine = new ConcurrentHashMap<>();
    @PostConstruct
    public void init() {
        this.actionEngine.put(MetaConstants.JobType.JOB_TYPE_BATCH, dataTransferAction);

        if (!ObjectUtils.isEmpty(transformDataTransferAction)) {
            // 配置了seatunnel client后加载计算引擎
            this.actionEngine.put(MetaConstants.JobType.JOB_TYPE_COMPUTE, transformDataTransferAction);
        }
    }

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

    @RequestMapping("/stream_health")
    public WebResult<String> streamHealth(String jobId) {
        // 如果因为datalinkx挂掉后重启，flink任务正常，datalinkx任务状态正常，判断健康检查线程是否挂掉, 如果挂掉，先停止再重新提交
        Set<Thread> threadsSet = Thread.getAllStackTraces().keySet();
        List<Thread> healthThreads = threadsSet.stream()
                .filter(Thread::isAlive)
                .filter(th -> th.getName().equals(IdUtils.getHealthThreadName(jobId)))
                .collect(Collectors.toList());

        if (ObjectUtils.isEmpty(healthThreads)) {
            return WebResult.of("");
        }
        return WebResult.of(healthThreads.get(0).getName());
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
        MDC.put("trace_id", new Date().getTime() + ":" + jobId);

        long startTime = new Date().getTime();
        DataTransJobDetail jobDetail;
        try {
            jobDetail = this.getJobDetail(jobId);
            AbstractDataTransferAction engine = this.actionEngine.get(jobDetail.getType());
            if (ObjectUtils.isEmpty(engine)) {
                throw new DatalinkXJobException("引擎加载失败，检查配置!");
            }
            engine.doAction(jobDetail);
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
