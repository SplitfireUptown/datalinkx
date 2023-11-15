package com.datalinkx.dataserver.controller.form;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 任务相关参数
 *
 * @author uptown
 * @create 01 27, 2021
 * @since 1.0.0
 */
@Data
public class DataTableForm {

    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataTableCreateBaseForm {
        @JsonProperty("tb_name")
        private String tbName;
        private String type;
        private String model;
        private String remark;
        private String ref;

        @JsonProperty("api_config")
        private ApiConfig apiConfig;

        private List<Field> fields;
        private Integer clean;
        private Integer dereplication;
        private Integer rows;
        @JsonProperty("partition_info")
        private PartitionInfo partitionInfo;
    }

    @Data
    public static class ApiConfig {
        private JsonNode body;
        private JsonNode code;

        private String url;
        private List<Value> variables;
        private List<Headers> headers;

        @JsonProperty("selected_keys")
        private List<SelectedKey> selectedKeys;

        @JsonProperty("parse_code_mode")
        private int parseCodeMode;
        private String method;

        @Data
        public static class Value {
            private String value;
            private String key;
        }

        @Data
        public static class Body {
            private String type;
            private List<Value> value;

            @JsonInclude(JsonInclude.Include.NON_NULL)
            @JsonIgnoreProperties(ignoreUnknown = true)
            private String rawType;
        }

        @Data
        public static class Code {
            @JsonProperty("pos_code")
            private String posCode;

            @JsonProperty("pre_code")
            private String preCode;

            @JsonProperty("parse_code")
            private String parseCode;
        }

        @Data
        public static class Headers {

            private String value;
            private String key;
        }

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public static class SelectedKey {
            private String position;
            private String type;
            private String name;
            private String title;
            private List<SelectedKey> sub;
        }
    }

    @Data
    public static class Field {
        private int type;
        private String name;
        private String remark;
        private String position;
    }



    @Data
    public static class DataTableCreateForm extends DataTableCreateBaseForm {
        @JsonProperty("db_id")
        private String dbId;
        @JsonProperty("user_id")
        private String userId;
    }

    @Data
    public static class DataTableRetrieveForm {
        @JsonProperty("table_id")
        private String tableId;
        @JsonProperty("user_id")
        private String userId;
    }

    @Data
    public static class DataTableListRetrieveForm {
        @JsonProperty("db_id")
        private String dbId;
        @JsonProperty("user_id")
        private String userId;
    }


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PartitionInfo {
        @JsonProperty(value = "pt_field")
        private String ptField;
        @JsonProperty(value = "pt_type")
        private String ptType;
        @JsonProperty(value = "unit")
        private String unit;
    }

    @Data
    public static class DataTableDependencyForm {
        @JsonProperty("table_id")
        private String tableId;
        @JsonProperty("user_id")
        private String userId;
    }



    @Data
    public static class DataTableStatisticsForm {
        @JsonProperty("user_id")
        private String userId;
    }

}
