package com.datalinkx.dataserver.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.client.HttpConstructor;
import com.datalinkx.dataserver.controller.form.DsForm;
import com.datalinkx.dataserver.controller.form.JobForm;
import com.datalinkx.dataserver.service.impl.DsServiceImpl;
import com.datalinkx.driver.dsdriver.base.model.DbTableField;
import io.swagger.annotations.Api;
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
@RequestMapping("/api/ds")
@Api(tags = "ds")
public class DsController {

	@Autowired
	private DsServiceImpl dsServiceImpl;


	@GetMapping("/page")
	public PageVo<List<DsBean>> dsPage(DsForm.DataSourcePageForm dataSourcePageForm) {
		return dsServiceImpl.dsPage(dataSourcePageForm);
	}

	@GetMapping("/list")
	public WebResult<List<DsBean>> list() {
		return WebResult.of(dsServiceImpl.list());
	}


	@GetMapping("/group")
	public WebResult<Map> group() {
		return WebResult.of(
				dsServiceImpl.list().stream().collect(Collectors.groupingBy(DsBean::getType,  Collectors.counting()))
		);
	}

	@PostMapping("/http/test")
	public WebResult httpTest(@RequestBody JobForm.HttpTestForm httpTestForm) {
		return WebResult.of(HttpConstructor.go(httpTestForm));
	}


	@GetMapping("/info/{dsId}")
	public WebResult<DsBean> info(@PathVariable String dsId) {
		try {
			return WebResult.of(dsServiceImpl.info(dsId));
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw new DatalinkXServerException(e);
		}
	}

	@PostMapping("/modify")
	public void modify(@RequestBody DsForm.DsCreateForm form) {
		this.dsServiceImpl.modify(form);
	}

	@PostMapping("/create")
	public WebResult<String> create(@RequestBody DsForm.DsCreateForm form) throws UnsupportedEncodingException {
		return WebResult.of(dsServiceImpl.create(form));
	}

	@PostMapping("/delete/{dsId}")
	public void del(@PathVariable String dsId) {
		dsServiceImpl.del(dsId);
	}

	@GetMapping("/tables/{dsId}")
	public WebResult<List<String>> fetchTables(@PathVariable String dsId) {
		return WebResult.of(dsServiceImpl.fetchTables(dsId));
	}

	@GetMapping("/field/info")
	public WebResult<List<DbTableField>> tbInfo(String dsId, String name) {
		return WebResult.of(dsServiceImpl.fetchFields(dsId, name));
	}
}
