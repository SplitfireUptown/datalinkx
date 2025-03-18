package com.datalinkx.datajob.action;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.exception.DatalinkXJobException;
import com.datalinkx.common.utils.ObjectUtils;
import com.datalinkx.compute.connector.jdbc.TransformNode;
import com.datalinkx.compute.transform.ITransformDriver;
import com.datalinkx.dataclient.client.seatunnel.request.ComputeJobGraph;
import com.datalinkx.compute.transform.ITransformFactory;
import com.datalinkx.dataclient.client.seatunnel.SeaTunnelClient;
import com.datalinkx.dataclient.client.seatunnel.response.JobCommitResp;
import com.datalinkx.dataclient.client.seatunnel.response.JobOverviewResp;
import com.datalinkx.datajob.bean.JobStateForm;
import com.datalinkx.datajob.bean.JobSyncModeForm;
import com.datalinkx.datajob.client.datalinkxserver.DatalinkXServerClient;
import com.datalinkx.driver.dsdriver.DsDriverFactory;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.datalinkx.driver.dsdriver.base.model.FlinkActionMeta;
import com.datalinkx.driver.dsdriver.base.model.SeatunnelActionMeta;
import com.datalinkx.driver.model.DataTransJobDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

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
        log.info(String.format("transform job jobid: %s, end to transfer", unit.getJobId()));
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
        if (1 == unit.getCover()) {
            unit.getDsWriter().truncateData(
                    FlinkActionMeta.builder()
                            .writer(unit.getWriter())
                            .build()
            );
        }
    }

    @Override
    protected void execute(SeatunnelActionMeta unit) throws Exception {
        ComputeJobGraph computeJobGraph = new ComputeJobGraph();
        computeJobGraph.setJobId(unit.getJobId());
        computeJobGraph.setEnv(new HashMap<String, Object>() {{
            put("job.mode", unit.getJobMode());
        }});
        computeJobGraph.setSource(Collections.singletonList(unit.getSourceInfo()));
        computeJobGraph.setTransform(unit.getTransformInfo().stream()
                .map(node -> (Object) node) // 使用 map 将 TransformNode 转换为 Object
                .collect(Collectors.toList()));
        computeJobGraph.setSink(Collections.singletonList(unit.getSinkInfo()));
        log.info("job_graph ==> {}", computeJobGraph);
        JobCommitResp jobCommitResp = seaTunnelClient.jobSubmit(computeJobGraph);
        String taskId = jobCommitResp.getJobId();
        unit.setTaskId(taskId);
        // 更新task
        datalinkXServerClient.updateJobTaskRel(unit.getJobId(), taskId);
    }

    @Override
    protected boolean checkResult(SeatunnelActionMeta unit) throws DatalinkXJobException {
        String taskId = unit.getTaskId();
        if (ObjectUtils.isEmpty(taskId)) {
            throw new DatalinkXJobException("task id is empty.");
        }

        JobOverviewResp jobOverviewResp = seaTunnelClient.jobOverview(taskId);
        if (MetaConstants.JobStatus.SEATUNNEL_JOB_FINISH.equalsIgnoreCase(jobOverviewResp.getJobStatus())) {
            return true;
        }

        if (MetaConstants.JobStatus.SEATUNNEL_JOB_FAILED.equalsIgnoreCase(jobOverviewResp.getJobStatus())) {
            String errorMsg = "请检查Seatunnel日志";
            if (!ObjectUtils.isEmpty(jobOverviewResp.getErrorMsg())) {
                errorMsg = jobOverviewResp.getErrorMsg();
            }
            log.error(errorMsg);
            throw new DatalinkXJobException(errorMsg);
        }

        return false;
    }

    @Override
    protected void afterExec(SeatunnelActionMeta unit, boolean success) {
        // 记录增量记录
        if (success) {
            datalinkXServerClient.updateSyncMode(
                    JobSyncModeForm.builder()
                            .jobId(unit.getJobId())
                            .increateValue(
                                    unit.getReader().getMaxValue()
                            ).build());
        }
    }

    @Override
    protected SeatunnelActionMeta convertExecUnit(DataTransJobDetail info) throws Exception {
        IDsReader dsReader = DsDriverFactory.getDsReader(info.getSyncUnit().getReader().getConnectId());
        IDsWriter dsWriter = DsDriverFactory.getDsWriter(info.getSyncUnit().getWriter().getConnectId());

        Map<String, Object> commonSettings = info.getSyncUnit().getCommonSettings();
        List<TransformNode> transformNodes = new ArrayList<>();
        String lastTransformNodeName = "";

        for (DataTransJobDetail.Compute.Transform transform : info.getSyncUnit().getCompute().getTransforms()) {
            ITransformDriver computeDriver = ITransformFactory.getComputeDriver(transform.getType());
            TransformNode transformNode = computeDriver.transferInfo(commonSettings, transform.getMeta());
            lastTransformNodeName = transformNode.getResultTableName();
            transformNodes.add(transformNode);
        }

        SeatunnelActionMeta seatunnelActionMeta = SeatunnelActionMeta.builder()
                .writer(info.getSyncUnit().getWriter())
                .reader(info.getSyncUnit().getReader())
                .dsWriter(dsWriter)
                .dsReader(dsReader)
                .transformInfo(transformNodes)
                .jobMode("batch")
                .jobId(info.getJobId())
                .cover(info.getCover())
                .parallelism(1)
                .build();

        TransformNode sinkInfo = dsWriter.getSinkInfo(seatunnelActionMeta);
        sinkInfo.setSourceTableName(lastTransformNodeName);


        seatunnelActionMeta.setSourceInfo(dsReader.getSourceInfo(seatunnelActionMeta));
        seatunnelActionMeta.setSinkInfo(sinkInfo);

        return seatunnelActionMeta;
    }
}
