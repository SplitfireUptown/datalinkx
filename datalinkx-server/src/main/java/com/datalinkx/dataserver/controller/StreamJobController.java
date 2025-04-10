package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.service.StreamJobService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/stream/job")
public class StreamJobController {

    @Autowired
    StreamJobService streamJobService;

    @ApiOperation("流式任务创建")
    @RequestMapping("/create")
    public WebResult<String> createStreamJob(@RequestBody JobForm.JobCreateForm form) {
        return WebResult.of(streamJobService.createStreamJob(form));
    }

    @ApiOperation("流式任务编辑")
    @RequestMapping("/modify")
    public WebResult<String> modifyStreamJob(@RequestBody JobForm.JobModifyForm form) {
        return WebResult.of(streamJobService.modifyStreamJob(form));
    }

    @ApiOperation("流式任务分页查询")
    @RequestMapping("/page")
    public PageVo<List<JobVo.JobStreamPageVo>> streamPage(JobForm.JobPageForm form) {
        return streamJobService.streamPage(form);
    }


    @ApiOperation("流转任务-删除")
    @PostMapping("/delete/{jobId}")
    public WebResult<String> delete(@PathVariable String jobId) {
        streamJobService.delete(jobId);
        return WebResult.of(jobId);
    }


    @ApiOperation("手动停止流式任务")
    @RequestMapping("/stop/{jobId}")
    public WebResult<String> streamJobStop(@PathVariable String jobId) {
        streamJobService.stop(jobId);
        return WebResult.of(jobId);
    }

    @ApiOperation("手动执行流式任务")
    @RequestMapping("/exec/{jobId}")
    public WebResult<String> streamJobExec(@PathVariable String jobId) {
        streamJobService.startStreamJob(jobId);
        return WebResult.of(jobId);
    }
}
