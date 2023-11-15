package com.datalinkx.dataserver.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import com.datalinkx.driver.dsdriver.base.model.TableField;
import com.datalinkx.dataserver.bean.domain.DsBean;
import com.datalinkx.dataserver.bean.vo.PageVo;
import com.datalinkx.dataserver.config.WebResult;
import com.datalinkx.dataserver.controller.form.DsForm;
import com.datalinkx.dataserver.service.impl.DsService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author uptown
 * @version 1.0
 * @create 12/8/20 3:46 下午
 **/
@Slf4j
@RestController
@RequestMapping("/api/ds")
@Api(tags = "ds")
public class DsController {

	@Autowired
	private DsService dsService;


	@GetMapping("/page")
	public PageVo<List<DsBean>> dsPage(DsForm.DataSourcePageForm dataSourcePageForm) {
		return dsService.dsPage(dataSourcePageForm);
	}

	@GetMapping("/list")
	public WebResult<List<DsBean>> list() {
		return WebResult.of(dsService.list());
	}

	@GetMapping("/info/{dsId}")
	public WebResult<DsBean> info(@PathVariable String dsId) {
		return WebResult.of(dsService.info(dsId));
	}

	@PostMapping("/modify")
	public void modify(@RequestBody DsForm.DsCreateForm form) {
		this.dsService.modify(form);
	}

	@PostMapping("/create")
	public WebResult<String> create(@RequestBody DsForm.DsCreateForm form) throws UnsupportedEncodingException {
		return WebResult.of(dsService.create(form));
	}

	@PostMapping("/delete/{dsId}")
	public void del(@PathVariable String dsId) {
		dsService.del(dsId);
	}

	@GetMapping("/tables/{dsId}")
	public WebResult<List<String>> fetchTables(@PathVariable String dsId) {
		return WebResult.of(dsService.fetchTables(dsId));
	}

	@GetMapping("/field/info")
	public WebResult<List<TableField>> tbInfo(String dsId, String name) {
		return WebResult.of(dsService.fetchFields(dsId, name));
	}
}
