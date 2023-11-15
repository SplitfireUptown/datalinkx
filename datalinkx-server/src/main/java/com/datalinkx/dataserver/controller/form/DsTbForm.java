package com.datalinkx.dataserver.controller.form;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

public class DsTbForm {

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class QueryDsForm {
        @JsonProperty("ds_name")
        private String dsName;

        @JsonProperty("ds_type")
        private Integer dsType;

        @JsonProperty("department_name")
        private String departmentName;

        @JsonProperty("system_name")
        private String systemName;

        @ApiModelProperty(value = "创建开始时间")
        @JsonProperty("create_start_time")
        private String createStartTime;

        @ApiModelProperty(value = "创建结束时间")
        @JsonProperty("create_end_time")
        private String createEndTime;

        @ApiModelProperty(value = "排序")
        @JsonProperty("order_by")
        private String orderBy = "ctime";

        @ApiModelProperty(value = "当前页")
        @JsonProperty("page_no")
        private Integer pageNo;

        @ApiModelProperty(value = "展示数量")
        @JsonProperty("page_size")
        private Integer pageSize;

        @ApiModelProperty(value = "升序或降序，ase升序；desc降序")
        @JsonProperty("order_direction")
        private String orderDirection = "desc";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class InfoDsForm {
        @JsonProperty("ds_id")
        private String dsId;

    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DeleteDsForm {
        @JsonProperty("ds_id_list")
        List<String> dsIdList;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class QueryXtbForm {
        @JsonProperty("job_group_name")
        private String jobGroupName;

        @JsonProperty("ds_id")
        private String dsId;

        @JsonProperty("ds_type")
        private String dsType;

        @JsonProperty("ds_name")
        private String dsName;

        @JsonProperty("xtb_name")
        private String xtbName;

        @ApiModelProperty(value = "有无校验")
        @JsonProperty("has_inspection_rules")
        private Integer hasInspectionRules;

        @ApiModelProperty(value = "排序")
        @JsonProperty("order_by")
        private String orderBy = "ctime";

        @ApiModelProperty(value = "当前页")
        @JsonProperty("page_no")
        private Integer pageNo;

        @ApiModelProperty(value = "展示数量")
        @JsonProperty("page_size")
        private Integer pageSize;

        @ApiModelProperty(value = "升序或降序，ase升序；desc降序")
        @JsonProperty("order_direction")
        private String orderDirection = "desc";
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DeleteXtbForm {
        @JsonProperty("xtb_id_list")
        List<String> xtbIdList;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class DsTreeForm {
        @JsonProperty("ds_id")
        private String dsId;

        @JsonProperty("parent")
        private String parent;
    }
}
