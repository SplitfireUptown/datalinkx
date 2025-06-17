package com.datalinkx.copilot.mcp.tools;

import com.datalinkx.common.constants.LLMPromptConstants;
import com.datalinkx.rpc.client.datalinkxserver.DatalinkXServerClient;
import lombok.extern.slf4j.Slf4j;
import org.noear.solon.ai.annotation.ToolMapping;
import org.noear.solon.annotation.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;



@Slf4j
@Service
public class DataTransferJobTool extends BaseMCPTool {

    @Autowired
    DatalinkXServerClient datalinkXServerClient;


    @ToolMapping(description = "删除任务")
    public String deleteByName(@Param String jobName) {
        return packageJob("删除任务", datalinkXServerClient.deleteJobByName(jobName));
    }


    @ToolMapping(description = "执行任务")
    public String triggerByName(@Param String jobName) {
        return packageJob("执行任务", datalinkXServerClient.triggerJobByName(jobName));
    }

    @ToolMapping(description = "查询任务")
    public String list() {
        return String.format("数据字段解释: %s; 数据：%s", LLMPromptConstants.JOB_LIST_SCHEMA_PROMPT, datalinkXServerClient.mcpJobList().getResult());
    }

    @ToolMapping(description = "任务详情")
    public String info(@Param String name) {
        return String.format("数据字段解释: %s; 数据：%s", LLMPromptConstants.JOB_INFO_SCHEMA_PROMPT, datalinkXServerClient.mcpJobInfo(name).getResult());
    }

    @ToolMapping(description = "任务级联配置，任务依赖关系")
    public String cascadeConfig(@Param(name = "jobName", description = "任务") String jobName, @Param(name = "subJobName", description = "子任务") String subJobName) {
        return packageJob("任务级联配置", datalinkXServerClient.mcpJobCascadeConfig(jobName, subJobName));
    }

    @ToolMapping(description = "删除任务级联配置，删除任务依赖关系")
    public String deleteCascadeConfig(@Param String jobName) {
        return packageJob("删除任务级联配置", datalinkXServerClient.mcpJobDeleteConfig(jobName));
    }
}
