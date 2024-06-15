package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.vo.SystemMonitorVo;
import com.datalinkx.dataserver.monitor.SystemMonitor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/monitor")
public class MonitorController {

    @GetMapping("/server/info")
    public WebResult<SystemMonitorVo> info() throws Exception {
        return WebResult.of(SystemMonitor.stat());
    }
}
