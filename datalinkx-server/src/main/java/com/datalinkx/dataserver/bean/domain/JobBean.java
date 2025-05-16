package com.datalinkx.dataserver.bean.domain;


import io.swagger.annotations.ApiModel;
import lombok.*;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
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
public class JobBean extends BaseDomainBean implements Serializable {
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
	@Column(name = "to_tb", columnDefinition = "char(40)")
	private String toTb;
	@Column(name = "from_tb", columnDefinition = "char(40)")
	private String fromTb;
	@Column(name = "xxl_id", columnDefinition = "char(40)")
	private String xxlId;
	@Column(name = "task_id", columnDefinition = "char(40)")
	private String taskId;
	@Column(name = "sync_mode", columnDefinition = "char(40)")
	private String syncMode;
	@Column(name = "`count`", columnDefinition = "text")
	private String count;
	// 0 - 新建、1 - 运行中、2 - 运行成功、3 - 运行失败、4 - 停止、5 - 队列中
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
	@Column(name = "retry_time")
	public Integer retryTime;
}
