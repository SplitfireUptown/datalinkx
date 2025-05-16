package com.datalinkx.dataserver.controller.form;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


public class DsForm {

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class DsCreateForm {
		@JsonProperty("ds_id")
		private String dsId;
		@ApiModelProperty(value = "数据源创建")
		@JsonProperty("name")
		private String name;
		private String host;
		private String username;
		private String password;
		@JsonProperty("database")
		private String database = "";
		private Integer port;
		private String type;
		private String config;
	}

	@Data
	public static class DataSourcePageForm {
		private String name;
		private String type;

		@ApiModelProperty(value = "当前页")
		@JsonProperty("page_no")
		private Integer pageNo = 1;

		@ApiModelProperty(value = "展示数量")
		@JsonProperty("page_size")
		private Integer pageSize = 10;
	}
}
