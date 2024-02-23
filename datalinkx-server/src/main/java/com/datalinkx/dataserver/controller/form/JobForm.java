package com.datalinkx.dataserver.controller.form;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


public class JobForm {

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class ConfigForm {
		// mysql/postgreSQL/es需要的参数
		private String name;
		// datahub的entPoint绑定在url上
		private String url;
		// datahub的accessId绑定在user上
		private String user;
		// datahub的accessKey绑定在password上
		private String password;
		// datahub的project绑定在dbName上
		@JsonProperty("db_name")
		private String dbName;
		// es需要额外配置的参数
		@JsonProperty("is_security")
		private Integer isSecurity;
		@JsonProperty("is_net_ssl")
		private Integer isNetSSL;

	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class JobCreateForm {
		@ApiModelProperty("任务名称")
		@JsonProperty("job_name")
		private String jobName;
		@ApiModelProperty(value = "数据源id")
		@JsonProperty("from_ds_id")
		private String fromDsId;
		@JsonProperty("to_ds_id")
		private String toDsId;
		@JsonProperty("from_tb_name")
		private String fromTbName;
		@ApiModelProperty(value = "导入表id")
		@JsonProperty("to_tb_name")
		private String toTbName;
		@ApiModelProperty(value = "定时策略")
		@JsonProperty("scheduler_conf")
		private String schedulerConf;
		@ApiModelProperty(value = "字段映射配置")
		@JsonProperty("sync_mode")
		private SyncModeForm syncMode;
		@JsonProperty("field_mappings")
		private List<FieldMappingForm> fieldMappings;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class JobModifyForm extends JobCreateForm {
		@JsonProperty("job_id")
		private String jobId;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class FieldMappingForm {
		@ApiModelProperty(value = "from表字段名称")
		private String sourceField;
		@ApiModelProperty(value = "to表字段名称")
		private String targetField;
		private String mappingValue = "string";
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static final class SyncModifyForm {
		@JsonProperty("job_id")
		private String jobId;
		@JsonProperty("increate_value")
		private String increateValue;
	}

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class SyncModeForm {
		@ApiModelProperty(value = "overwrite全量更新，increment增量更新")
		private String mode = "overwrite";
		@JsonProperty("increate_field")
		private String increateField;
		@JsonProperty("increate_value")
		private String increateValue = "";
	}

	@Data
	public static class JobPageForm {

		@ApiModelProperty(value = "当前页")
		@JsonProperty("page_no")
		private Integer pageNo = 1;

		@ApiModelProperty(value = "展示数量")
		@JsonProperty("page_size")
		private Integer pageSize = 10;
	}

	@EqualsAndHashCode(callSuper = true)
	@Data
	public static class JobLogPageForm extends JobPageForm {
		@JsonProperty("job_id")
		private String jobId;
	}

	@Data
	public static class JobRelationForm {
		@JsonProperty("job_id")
		private String jobId;
		@JsonProperty("sub_job_id")
		private String subJobId;
		private Integer priority;
	}
}
