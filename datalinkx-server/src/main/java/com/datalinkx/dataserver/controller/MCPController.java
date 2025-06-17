package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.common.utils.JsonUtils;
import com.datalinkx.dataserver.bean.vo.DsVo;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.service.impl.DsServiceImpl;
import com.datalinkx.dataserver.service.impl.JobRelationServiceImpl;
import com.datalinkx.dataserver.service.impl.JobServiceImpl;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


@RequestMapping("/api/mcp")
@RestController
public class MCPController {

    @Autowired
    private JobServiceImpl jobServiceImpl;

    @Autowired
    private DsServiceImpl dsServiceImpl;


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
    public WebResult<String> jobList() {
        return WebResult.of(jobServiceImpl.mcpJobList());
    }

    @ApiOperation("流转任务-列表")
    @GetMapping("/job/info")
    public WebResult<String> jobInfo(@RequestParam String name) {
        return WebResult.of(jobServiceImpl.mcpJobInfo(name));
    }

    @ApiOperation("流转任务-级联配置")
    @PostMapping("/job/cascade_config")
    public WebResult<String> cascadeConfig(@RequestParam String jobName, @RequestParam String subJobName) {
        return WebResult.of(jobServiceImpl.mcpJobCascadeConfig(jobName, subJobName));
    }


    @ApiOperation("流转任务-删除级联配置")
    @PostMapping("/job/cascade_delete")
    public WebResult<String> cascadeDelete(@RequestParam String jobName) {
        return WebResult.of(jobServiceImpl.mcpJobCascadeDelete(jobName));
    }


    @ApiOperation("数据源-列表")
    @GetMapping("/ds/list")
    public WebResult<String> dsList() {
        List<DsVo.MCPDsList> dsVoList = dsServiceImpl.list().stream().map(
                dsBean -> new DsVo.MCPDsList(dsBean.getName(), dsBean.getType(), dsBean.getHost(), dsBean.getPort())).collect(Collectors.toList());
        return WebResult.of(JsonUtils.toJson(dsVoList));
    }

    @ApiOperation("数据源-列表")
    @GetMapping("/ds/info")
    public WebResult<String> dsInfo(@RequestParam String name) {
        return WebResult.of(dsServiceImpl.mcpInfo(name));
    }
}
