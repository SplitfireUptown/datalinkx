package com.datalinkx.dataserver.bean.domain;


import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@ApiModel(description = "流转任务")
@Data
@FieldNameConstants
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "JOB")
public class JobBean extends BaseDomainBean {
	private static final long serialVersionUID = 1L;
	@NotBlank
	@Column(name = "name", nullable = false, length = 64, columnDefinition = "char(64)")
	private String name;
	@NotBlank
	@Column(name = "job_id", nullable = false, length = 40, columnDefinition = "char(40)")
	private String jobId;
	@Column(name = "reader_ds_id", nullable = false, length = 40, columnDefinition = "char(40)")
	private String readerDsId;
	@Column(name = "writer_ds_id", nullable = false, length = 40, columnDefinition = "char(40)")
	private String writerDsId;
	@NotBlank
	@Column(name = "config", nullable = false, columnDefinition = "longtext")
	private String config;
	@Column(name = "crontab", length = 256, columnDefinition = "varchar(256)")
	private String crontab;
	@Column(name = "to_tb_id", columnDefinition = "char(40)")
	private String toTbId;
	@Column(name = "from_tb_id", columnDefinition = "char(40)")
	private String fromTbId;
	@Column(name = "xxl_id", columnDefinition = "char(40)")
	private String xxlId;
	@Column(name = "task_id", columnDefinition = "char(40)")
	private String taskId;
	@Column(name = "sync_mode", columnDefinition = "char(40)")
	private String syncMode;
	@Column(name = "`count`", columnDefinition = "text")
	private String count;
	// CREATE = 0; SYNCING = 1; NORMAL = 2; ERROR = 3; QUEUE = 4; STOP = 5
	@Column(name = "status", nullable = false, columnDefinition = "int(2)")
	private Integer status;
	@Column(name = "error_msg", columnDefinition = "longtext")
	private String errorMsg;
	// 流式任务断点续传
	@Column(name = "checkpoint", columnDefinition = "longtext")
	private String checkpoint;
	@Column(name = "graph", columnDefinition = "longtext")
	private String graph;
	// 0 不覆盖 1 覆盖
	@Column(name = "cover")
	private Integer cover;
	// 0 批式任务 1 流式任务 2 计算任务
	@Column(name = "type")
	private Integer type;
	@Column(name = "start_time")
	public Timestamp startTime;
}
