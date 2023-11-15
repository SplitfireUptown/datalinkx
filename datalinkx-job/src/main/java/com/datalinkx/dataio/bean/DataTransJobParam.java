package com.datalinkx.dataio.bean;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;

/**
 * @author uptown
 * @Description TODO
 * @createTime 2021年05月27日 10:19:31
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldNameConstants
@ToString(callSuper = true)
public class DataTransJobParam {
    String jobId;  // 导入schedulerId，导出为jobId
    Integer full;
}
