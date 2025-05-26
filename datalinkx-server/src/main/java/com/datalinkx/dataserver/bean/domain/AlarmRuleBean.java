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
import java.sql.Timestamp;

@EqualsAndHashCode(callSuper = true)
@ApiModel(description = "告警规则")
@Data
@SuperBuilder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@DynamicInsert
@DynamicUpdate
@Entity
@Table(name = "ALARM_RULE")
public class AlarmRuleBean extends BaseDomainBean {

    @Column(name = "rule_id", nullable = false, length = 40, columnDefinition = "char(40)")
    private String ruleId;

    @Column(name = "name", nullable = false, length = 40, columnDefinition = "char(40)")
    private String name;
    // 告警规则id
    @Column(name = "alarm_id", nullable = false, length = 40, columnDefinition = "char(40)")
    private String alarmId;

    // 流转任务id
    @Column(name = "job_id", nullable = false, length = 40, columnDefinition = "char(40)")
    private String jobId;

    // 0 - 无论成功失败，任务结束后就推送 1 - 失败才推送 2 - 成功才推送
    @Column(name = "type", nullable = false)
    private Integer type;

    // 0 - 禁用 1 - 启用
    @Column(name = "status", nullable = false)
    private Integer status;

    // 最近一次推送时间
    @Column(name = "push_time", nullable = false)
    private Timestamp pushTime;
}
