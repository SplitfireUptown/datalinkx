package com.datalinkx.dataserver.bean.vo;

import java.sql.Timestamp;
import java.util.List;

import com.datalinkx.dataserver.controller.form.JobForm;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



public class JobVo {

	@Data
	@ApiModel
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class JobPageVo {
		@ApiModelProperty(value = "任务id")
		@JsonProperty("job_id")
		private String jobId;
		@ApiModelProperty(value = "任务id")
		@JsonProperty("job_name")
		private String jobName;

		@JsonProperty("from_tb_name")
		private String fromTbName;

		@JsonProperty("to_tb_name")
		private String toTbName;

		private String progress;

		private Integer status;
	}

	@Data
	@ApiModel
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class JobLogPageVo {
		@ApiModelProperty(value = "任务id")
		@JsonProperty("job_id")
		private String jobId;

		@JsonProperty("error_msg")
		private String errorMsg;

		@JsonProperty("start_time")
		private Timestamp startTime;

		@JsonProperty("end_time")
		private Timestamp endTime;

		@JsonProperty("cost_time")
		private Integer costTime;

		@JsonProperty("append_count")
		private Long appendCount;

		private Integer status;
	}

	@Data
	@ApiModel
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class JobInfoVo {
		@ApiModelProperty(value = "任务id")
		@JsonProperty("job_id")
		private String jobId;
		@ApiModelProperty(value = "任务id")
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
		@ApiModelProperty(value = "数据覆盖")
		private Integer cover;
		@ApiModelProperty(value = "字段映射配置")
		@JsonProperty("field_mappings")
		private List<JobForm.FieldMappingForm> fieldMappings;
		@JsonProperty("sync_mode")
		private JobForm.SyncModeForm syncMode;
	}


	@Data
	@ApiModel
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class FieldMappingInnerVo {

		@ApiModelProperty(value = "原始字段")
		@JsonProperty("originalName")
		private String originalName;
		@ApiModelProperty(value = "映射字段")
		@JsonProperty("mappingName")
		private String mappingName;
		@ApiModelProperty(value = "映射字段类型")
		@JsonProperty("mappingValue")
		private String mappingValue = "";

	}

	@Data
	@ApiModel
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class JobId2NameVo {
		@ApiModelProperty(value = "任务id")
		@JsonProperty("job_id")
		private String JobId;
		@ApiModelProperty(value = "任务名称")
		@JsonProperty("job_name")
		private String jobName;
	}

	@Data
	@ApiModel
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class JobRelationVo {
		@JsonProperty("relation_id")
		private String relationId;
		@JsonProperty("job_id")
		private String jobId;
		@JsonProperty("job_name")
		private String jobName;
		@JsonProperty("sub_job_id")
		private String subJobId;
		@JsonProperty("sub_job_name")
		private String subJobName;
		private int priority;
	}

	@Data
	@ApiModel
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class JobRelationBloodVo {
		List<JobRelationBloodVoEdge> edges;
		List<JobRelationBloodVoNode> nodes;
	}

	@Data
	@ApiModel
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class JobRelationBloodVoNode {
		private String id;
		private String label;
	}

	@Data
	@ApiModel
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static final class JobRelationBloodVoEdge {
		private String from;
		private String to;
	}
}
