package com.datalinkx.driver.dsdriver.base.model;

import com.datalinkx.compute.model.TransformNode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class SeatunnelActionMeta extends EngineActionMeta {
    private String sourceInfo;
    private String sinkInfo;
    // 计算任务的中间节点
    private String transformInfo;
    private String jobMode;
    private Integer parallelism;
}
