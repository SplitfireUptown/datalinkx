package com.datalinkx.dataserver.controller.form;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


public class DsForm {

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class DsCreateForm {
		private String dsId;
		@ApiModelProperty(value = "导出数据源创建")
		@JsonProperty("name")
		private String name;
		private String host;
		private String username;
		private String password;
		@JsonProperty("database")
		private String database = "";
		private Integer port;
		private Integer type;
//		private ConfigForm config;
		@JsonProperty("tb_name_list")
		private List<String> tbNameList;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class ConfigForm {
		private String schema;
		// es需要额外配置的参数
		@JsonProperty("is_security")
		private Integer isSecurity;
		@JsonProperty("is_net_ssl")
		private Integer isNetSSL;
	}

	@Data
	public static class DataSourcePageForm {
		private String name;

		@ApiModelProperty(value = "当前页")
		@JsonProperty("page_no")
		private Integer pageNo = 1;

		@ApiModelProperty(value = "展示数量")
		@JsonProperty("page_size")
		private Integer pageSize = 10;
	}
}
