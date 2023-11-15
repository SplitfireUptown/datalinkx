package com.datalinkx.dataserver.bean.domain;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
	@Column(name = "error_msg", nullable = true, columnDefinition = "longtext")
	private String errorMsg;
}
