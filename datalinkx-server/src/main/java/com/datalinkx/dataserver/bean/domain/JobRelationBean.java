package com.datalinkx.dataserver.bean.domain;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@ApiModel(description = "流转任务")
@Entity
@DynamicUpdate
@DynamicInsert
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "JOB_RELATION")
public class JobRelationBean extends BaseDomainBean implements Serializable {
    private static final long serialVersionUID = 1L;
    @Column(name = "relation_id", nullable = false, length = 40, columnDefinition = "char(40)")
    private String relationId;
    @NotBlank
    @Column(name = "job_id", nullable = false, length = 40, columnDefinition = "char(40)")
    private String jobId;
    @NotBlank
    @Column(name = "sub_job_id", nullable = false, length = 40, columnDefinition = "char(40)")
    private String subJobId;
    @Column(name = "priority", nullable = false, length = 40, columnDefinition = "char(40)")
    private int priority;
}
