package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.vo.SystemMonitorVo;
import com.datalinkx.dataserver.monitor.SystemMonitor;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @GetMapping("/server/info")
    public WebResult<SystemMonitorVo> info() throws Exception {
        return WebResult.of(SystemMonitor.stat());
    }

    @GetMapping("/city/mock")
    public WebResult<Object> mock() throws Exception {
        Resource resource = new DefaultResourceLoader().getResource("city_info.json");

        InputStream inputStream = resource.getInputStream();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            return WebResult.of(JsonUtils.toJsonNode(content.toString()));
        }
    }
}
