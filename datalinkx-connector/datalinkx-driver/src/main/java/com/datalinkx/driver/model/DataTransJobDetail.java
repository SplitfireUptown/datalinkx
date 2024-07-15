package com.datalinkx.driver.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldNameConstants;



@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Builder
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataTransJobDetail {
    @JsonProperty("job_id")
    String jobId;
    @JsonProperty("lock_id")
    String lockId;
    @JsonProperty("sync_unit")
    @JsonIgnoreProperties(ignoreUnknown = true)
    SyncUnit syncUnit;
    Integer cover;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SyncUnit {
        Reader reader;
        Writer writer;
        String checkpoint;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Reader {
        String catalog;
        String schema;
        String tableName;
        List<Column> columns;
        Sync sync;
        String connectId;
        String type;
        String maxValue;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Writer {
        String catalog;
        String schema;
        String tableName;
        List<Column> columns;
        String connectId;
        Integer batchSize;
        String type;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Column {
        String name;
        String type;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Sync {
        String type; // [overwrite全量更新，increment增量更新]
        Integer fetchSize = 10000; // 每次获取的条数
        Integer queryTimeOut = 10000; // flinkx查询执行超时时间
        SyncCondition syncCondition;

        @Data
        @AllArgsConstructor
        @NoArgsConstructor
        @Builder
        public static class SyncCondition {
            String field;
            String fieldType;
            Conditon start;
            Conditon end;

            @Data
            @AllArgsConstructor
            @NoArgsConstructor
            @Builder
            public static class Conditon {
                String operator; // 算子，比如 >=, <=
                Integer enable;
                String value;
            }
        }
    }

}
