package com.datalinkx.dataserver.controller;

import java.util.List;

import com.datalinkx.dataserver.bean.vo.JobVo;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.service.impl.JobService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/job")
public class JobController {

	@Autowired
	private JobService jobService;

	@PostMapping("/create")
	public WebResult<String> jobCreate(@RequestBody JobForm.JobCreateForm form) {
		return WebResult.of(jobService.jobCreateProxy(form));
	}

	@PostMapping("/modify")
	public WebResult<String> jobModify(@RequestBody JobForm.JobModifyForm form) {
		return WebResult.of(jobService.jobModify(form));
	}


	@PostMapping("/delete/{jobId}")
	public WebResult<String> delete(@PathVariable String jobId) {
		jobService.del(jobId);
		return WebResult.of(jobId);
	}

	@GetMapping("/page")
	public PageVo<List<JobVo.JobPageVo>> page(JobForm.JobPageForm form) {
		return jobService.page(form);
	}

	@GetMapping("/info/{jobId}")
	public WebResult<JobVo.JobInfoVo> info(@PathVariable String jobId) {
		return WebResult.of(jobService.info(jobId));
	}

	@RequestMapping("/exec/{jobId}")
	public WebResult<String> jobExec(@PathVariable String jobId) {
		jobService.jobExec(jobId);
		return WebResult.of(jobId);
	}

	@RequestMapping("/log/page")
	public PageVo<List<JobVo.JobLogPageVo>> logPage(JobForm.JobLogPageForm jobLogPageForm) {
		return jobService.logPage(jobLogPageForm);
	}
}
