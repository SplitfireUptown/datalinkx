package com.datalinkx.copilot.mcp.tools;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.rpc.client.datalinkxserver.DatalinkXServerClient;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.annotation.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Slf4j
@Service
public class DataTransferJobTool {

    @Autowired
    DatalinkXServerClient datalinkXServerClient;


    @ToolMapping(description = "删除任务")
    public String deleteByName(@Param() String jobName) {
        return packageJob("删除任务", datalinkXServerClient.triggerJobByName(jobName));
    }


    @ToolMapping(description = "执行任务")
    public String triggerByName(@Param String jobName) {
        return packageJob("执行任务", datalinkXServerClient.triggerJobByName(jobName));
    }

    @ToolMapping(description = "查询任务")
    public String list() {
        return datalinkXServerClient.jobList().getResult();
    }

    @ToolMapping(description = "任务详情")
    public String info(@Param String name) {
        return datalinkXServerClient.jobInfo(name).getResult();
    }


    public String packageJob(String operatorStr, WebResult<String> result) {
        try {
            if (!Objects.equals(result.getStatus(), "0")) {
                return operatorStr + "失败! " + result.getErrstr();
            }

            return result.getResult();
        } catch (Exception ex) {
            log.error(operatorStr + "失败！", ex);
            return operatorStr + "失败！需检查系统是否正常";
        }
    }
}
