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
public class DataSourceConfigTool extends BaseMCPTool {

    @Autowired
    DatalinkXServerClient datalinkXServerClient;


    @ToolMapping(description = "数据源列表")
    public String list() {
        return String.format("数据字段解释: %s; 数据：%s", LLMPromptConstants.DS_LIST_SCHEMA_PROMPT, datalinkXServerClient.mcpDsList().getResult());
    }

    @ToolMapping(description = "数据源详情")
    public String info(@Param("name") String name) {
        return String.format("数据字段解释: %s; 数据：%s", LLMPromptConstants.DS_INFO_SCHEMA_PROMPT, datalinkXServerClient.mcpDsInfo(name).getResult());
    }
}
