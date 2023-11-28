package com.datalinkx.dataserver.bean.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.datalinkx.dataserver.bean.domain.BaseDomainBean;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
@ApiModel(description = "数据源")
@Data
@FieldNameConstants
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "DS")
public class DsBean extends BaseDomainBean {

	private static final long serialVersionUID = 1L;
	@Column(name = "ds_id", nullable = true, length = 35, columnDefinition = "char(35)")
	private String dsId;
	@Column(name = "`name`", nullable = true, length = 64, columnDefinition = "varchar(64)")
	private String name;
	@Column(name = "type", nullable = true, columnDefinition = "int(11)")
	private Integer type;
	@Column(name = "`host`", nullable = true, length = 64, columnDefinition = "varchar(64)")
	private String host;
	@Column(name = "`port`", nullable = true, columnDefinition = "int(11)")
	private Integer port;
	@Column(name = "username", nullable = true, length = 128, columnDefinition = "varchar(128)")
	private String username;
	@Column(name = "`password`", nullable = true, length = 256, columnDefinition = "varchar(256)")
	private String password;
	@Column(name = "config", nullable = true, length = 65535, columnDefinition = "text")
	private String config;
//	@Column(name = "ds_desc", nullable = true, columnDefinition = "varchar(256)")
//	private String dsDesc;
	@Column(name = "`database`", nullable = true, length = 64, columnDefinition = "varchar(64)")
	private String database;
	@Column(name = "`schema`", nullable = true, length = 64, columnDefinition = "varchar(64)")
	private String schema;
}
