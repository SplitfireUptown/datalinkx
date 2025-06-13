package com.datalinkx.copilot.mcp.tools;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.rpc.client.datalinkxserver.DatalinkXServerClient;
import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.annotation.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DataTransferJobTool {

    @Autowired
    DatalinkXServerClient datalinkXServerClient;


    @ToolMapping(description = "删除任务")
    public String deleteByName(@Param String jobName) {
        WebResult<String> result = datalinkXServerClient.deleteJobByName(jobName);
        return result.getResult();
    }


    @ToolMapping(description = "执行任务")
    public String triggerByName(@Param String jobName) {
        WebResult<String> result = datalinkXServerClient.triggerJobByName(jobName);
        return result.getResult();
    }
}
