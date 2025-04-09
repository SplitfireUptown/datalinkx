package com.datalinkx.driver.dsdriver.base.model;

import java.util.Map;
import java.util.stream.Collectors;

import com.datalinkx.common.result.DatalinkXJobDetail;
import com.datalinkx.driver.dsdriver.IDsReader;
import com.datalinkx.driver.dsdriver.IDsWriter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldNameConstants;
import lombok.experimental.SuperBuilder;

/**
 * 执行引擎任务类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldNameConstants
@JsonIgnoreProperties(ignoreUnknown = true)
public class FlinkActionMeta extends EngineActionMeta {
    // datalinkx-server业务来源库信息
    public DatalinkXJobDetail.Reader reader;
    // datalinkx-server业务目标库信息
    public DatalinkXJobDetail.Writer writer;
    // 来源库driver驱动
    public IDsReader dsReader;
    // 目标库driver驱动
    public IDsWriter dsWriter;
    // 是否覆盖 0 - 否 1 - 是
    public Integer cover;
    // 数据总读行数
    public int readRecords = 0;
    // 数据总写行数 = readRecords - errorRecords
    public int writeRecords = 0;
    // 写字节数
    public long writeBytes;
}
