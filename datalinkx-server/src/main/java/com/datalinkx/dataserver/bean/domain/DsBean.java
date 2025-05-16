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
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
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
public class DsBean extends BaseDomainBean implements Serializable {

	private static final long serialVersionUID = 1L;
	@Column(name = "ds_id", length = 35, columnDefinition = "char(35)")
	private String dsId;
	@Column(name = "`name`", length = 64, columnDefinition = "varchar(64)")
	private String name;
	@Column(name = "type", columnDefinition = "int(11)")
	private String type;
	@Column(name = "`host`", length = 64, columnDefinition = "varchar(64)")
	private String host;
	@Column(name = "`port`", columnDefinition = "int(11)")
	private Integer port;
	@Column(name = "username", length = 128, columnDefinition = "varchar(128)")
	private String username;
	@Column(name = "`password`", length = 256, columnDefinition = "varchar(256)")
	private String password;
	@Column(name = "config", length = 65535, columnDefinition = "text")
	private String config;
	@Column(name = "`database`", length = 64, columnDefinition = "varchar(64)")
	private String database;
	@Column(name = "`schema`", length = 64, columnDefinition = "varchar(64)")
	private String schema;
}
