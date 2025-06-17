package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.service.impl.JobRelationServiceImpl;
import com.datalinkx.dataserver.service.impl.JobServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/mcp")
@RestController
public class MCPController {

    @Autowired
    private JobServiceImpl jobServiceImpl;

    @ApiOperation("流转任务-删除")
    @PostMapping("/job/delete_by_name")
    public WebResult<String> delete_by_name(@RequestParam String name) {
        String result = jobServiceImpl.mcpDelByName(name);
        return WebResult.of("删除成功: " + result);
    }

    @ApiOperation("流转任务-触发")
    @PostMapping("/job/trigger_by_name")
    public WebResult<String> trigger_by_name(@RequestParam String name) {
        String result = jobServiceImpl.mcpJobExecByName(name);
        return WebResult.of("触发成功：" + result);
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
