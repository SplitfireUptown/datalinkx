package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.service.impl.JobRelationServiceImpl;
import com.datalinkx.dataserver.service.impl.JobServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/mcp")
@RestController
public class MCPController {

    @Autowired
    private JobServiceImpl jobServiceImpl;

    @ApiOperation("流转任务-删除")
    @PostMapping("/job/delete_by_name")
    public WebResult<String> delete_by_name(@RequestParam String name) {
        return WebResult.of(jobServiceImpl.delByName(name));
    }

    @ApiOperation("流转任务-删除")
    @PostMapping("/job/trigger_by_name")
    public WebResult<String> trigger_by_name(@RequestParam String name) {
        return WebResult.of(jobServiceImpl.jobExecByName(name));
    }
}
