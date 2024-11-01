package com.datalinkx.datajob.action;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.dataclient.client.seatunnel.SeaTunnelClient;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SUCCESS;

@Slf4j
@Component
@ConditionalOnBean(SeaTunnelClient.class)
public class DataTransferComputeAction extends DataTransferAction {
    public static ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Autowired
    SeaTunnelClient seaTunnelClient;
    @Autowired
    DatalinkXServerClient datalinkXServerClient;

    @Override
    protected void begin(DataTransJobDetail info) {
        // 修改任务状态
        START_TIME.set(new Date().getTime());
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(info.getJobId())
                .jobStatus(MetaConstants.JobStatus.JOB_STATUS_SYNCING).startTime(START_TIME.get())
                .build());
    }

    @Override
    protected void end(FlinkActionMeta unit, int status, String errmsg) {
        log.info(String.format("jobid: %s, end to transfer", unit.getJobId()));
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(unit.getJobId())
                .jobStatus(status).startTime(START_TIME.get()).endTime(new Date().getTime())
                .errmsg(errmsg)
                .build());
        // 父任务执行成功后级联触发子任务
        if (JOB_STATUS_SUCCESS == status) {
            datalinkXServerClient.cascadeJob(unit.getJobId());
        }
    }

    @Override
    protected void execute(FlinkActionMeta unit) throws Exception {
        super.execute(unit);
    }

    @Override
    protected boolean checkResult(FlinkActionMeta unitParam) throws DatalinkXJobException {
        return super.checkResult(unitParam);
    }

    @Override
    protected void afterExec(FlinkActionMeta unit, boolean success) {
        super.afterExec(unit, success);
    }

    @Override
    protected FlinkActionMeta convertExecUnit(DataTransJobDetail jobDetail) {
        return super.convertExecUnit(jobDetail);
    }
}
