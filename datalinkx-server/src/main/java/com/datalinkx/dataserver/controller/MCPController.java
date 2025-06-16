package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.service.impl.JobRelationServiceImpl;
import com.datalinkx.dataserver.service.impl.JobServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/mcp")
@RestController
public class MCPController {

    @Autowired
    private JobServiceImpl jobServiceImpl;

    @ApiOperation("流转任务-删除")
    @PostMapping("/job/delete_by_name")
    public WebResult<String> delete_by_name(@RequestParam String name) {
        return WebResult.of(jobServiceImpl.mcpDelByName(name));
    }

    @ApiOperation("流转任务-删除")
    @PostMapping("/job/trigger_by_name")
    public WebResult<String> trigger_by_name(@RequestParam String name) {
        return WebResult.of(jobServiceImpl.mcpJobExecByName(name));
    }

    @ApiOperation("流转任务-列表")
    @GetMapping("/job/list")
    public WebResult<String> list() {
        return WebResult.of(jobServiceImpl.mcpJobList());
    }

    @ApiOperation("流转任务-列表")
    @GetMapping("/job/info")
    public WebResult<String> info(@RequestParam String name) {
        return WebResult.of(jobServiceImpl.mcpJobInfo(name));
    }
}
