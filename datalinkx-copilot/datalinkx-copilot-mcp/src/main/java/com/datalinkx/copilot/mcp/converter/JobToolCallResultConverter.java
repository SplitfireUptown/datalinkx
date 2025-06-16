package com.datalinkx.copilot.mcp.converter;

import org.noear.solon.ai.chat.tool.ToolCallResultConverter;
import org.noear.solon.core.exception.ConvertException;

public class JobToolCallResultConverter implements ToolCallResultConverter {

    public boolean matched(String mimeType) {
        return true;
    }


    @Override
    public String convert(Object result) throws ConvertException {
        return "";
    }
}
