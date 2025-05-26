package com.datalinkx.dataserver.bean.domain;


import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "告警组件配置")
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "ALARM_COMPONENT")
public class AlarmComponentBean extends BaseDomainBean {

    // 告警组件id
    @Column(name = "alarm_id", nullable = false, length = 40, columnDefinition = "char(40)")
    private String alarmId;

    @Column(name = "name", nullable = false, length = 40, columnDefinition = "char(40)")
    private String name;

    // 0 - 邮件 1 - 钉钉
    @Column(name = "type", nullable = false, length = 10, columnDefinition = "char(10)")
    private Integer type;

    // 告警组件配置
    @Column(name = "config", nullable = false, columnDefinition = "text")
    private String config;
}
