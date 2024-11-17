package com.datalinkx.datajob.action;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.compute.transform.ITransformFactory;
import com.datalinkx.dataclient.client.seatunnel.SeaTunnelClient;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.model.SeatunnelActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.Date;

import static com.datalinkx.common.constants.MetaConstants.JobStatus.JOB_STATUS_SUCCESS;


@Slf4j
@Component
@DependsOn("seaTunnelClient")
public class TransformDataTransferAction extends AbstractDataTransferAction<DataTransJobDetail, SeatunnelActionMeta> {
    public static ThreadLocal<Long> START_TIME = new ThreadLocal<>();

    @Autowired
    SeaTunnelClient seaTunnelClient;
    @Autowired
    DatalinkXServerClient datalinkXServerClient;

    @Override
    protected void begin(DataTransJobDetail info) {
        START_TIME.set(new Date().getTime());
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(info.getJobId())
                .jobStatus(MetaConstants.JobStatus.JOB_STATUS_SYNCING).startTime(START_TIME.get())
                .build());
    }

    @Override
    protected void end(SeatunnelActionMeta unit, int status, String errmsg) {
        log.info(String.format("stream job jobid: %s, end to transfer", unit.getJobId()));
        // 修改任务状态，存储checkpoint
        datalinkXServerClient.updateJobStatus(JobStateForm.builder().jobId(unit.getJobId())
                .jobStatus(status).endTime(new Date().getTime()).startTime(START_TIME.get())
                .errmsg(errmsg)
                .build());

        // 父任务执行成功后级联触发子任务
        if (JOB_STATUS_SUCCESS == status) {
            datalinkXServerClient.cascadeJob(unit.getJobId());
        }
    }

    @Override
    protected void beforeExec(SeatunnelActionMeta unit) throws Exception {
        unit.getWriterDsDriver().truncateData(
                FlinkActionMeta.builder()
                        .dsWriter(unit.getWriterDsDriver())
                        .build()
        );
    }

    @Override
    protected void execute(SeatunnelActionMeta unit) throws Exception {
//        seaTunnelClient.jobSubmit();
    }

    @Override
    protected boolean checkResult(SeatunnelActionMeta unit) {
        return false;
    }

    @Override
    protected void afterExec(SeatunnelActionMeta unit, boolean success) {

    }

    @Override
    protected SeatunnelActionMeta convertExecUnit(DataTransJobDetail info) throws Exception {
        IDsReader dsReader = DsDriverFactory.getDsReader(info.getSyncUnit().getReader().getConnectId());
        IDsWriter dsWriter = DsDriverFactory.getDsWriter(info.getSyncUnit().getWriter().getConnectId());


        return SeatunnelActionMeta.builder()
                .writerDsDriver(dsWriter)
                .sourceInfo(dsReader.getSourceInfo(info.getSyncUnit().getReader()).toString())
                .sinkInfo(dsWriter.getSinkInfo(info.getSyncUnit().getWriter()).toString())
                .transformInfo(ITransformFactory.getComputeDriver(info.getSyncUnit().getCompute().getType()).transferInfo(info.getSyncUnit().getCompute().getMeta()).toString())
                .jobMode("batch")
                .parallelism(1)
                .build();
    }
}
