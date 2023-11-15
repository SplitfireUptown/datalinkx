package com.datalinkx.driver.dsdriver.base.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TableField {
    String id;
    String name;
    String remark;
    String type;
    @JsonProperty("raw_type")
    String rawType;
    @JsonProperty("uniq_index")
    Boolean uniqIndex = false;
    Integer seqNo;
    Boolean allowNull;
}
