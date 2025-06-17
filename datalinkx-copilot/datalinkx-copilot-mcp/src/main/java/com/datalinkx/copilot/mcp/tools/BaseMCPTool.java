package com.datalinkx.copilot.mcp.tools;

import com.datalinkx.common.result.WebResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class BaseMCPTool {

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
