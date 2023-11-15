package com.datalinkx.driver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldNameConstants;

import java.util.List;


/**
 * @author uptown
 * @Description TODO
 * @createTime 2021年05月25日 19:55:59
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldNameConstants
@Builder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataTransJobDetail extends JobDetail {
    String jobId;
    List<SyncUnit> syncUnits;
    Integer full;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SyncUnit {
        String taskId;
        Reader reader;
        Writer writer;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Reader {
        String catalog;
        String schema;
        String tableId;
        String tableName;
        String realName;
        List<Column> columns;
        Sync sync;
        String connectId;
        String type;
        String maxValue;
        String remark;
        Integer tableComment;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Writer {
        String dbId; // dmc import db id
        String catalog; // jdbc catalog; dmc folder id
        String schema; // jdbc catalog; dmc folder name
        String tableId;
        String tableName;
        String realName;
        List<Column> columns;
        List<String> rowkey;
        String connectId;
        String type;
        String method;
        String url;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Column {
        String name;
        String type;
        String value;
        String format;
        Integer seqNo = 0;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Sync {
        String type; // [overwrite全量更新，increment增量更新]
        Integer fetchSize; // 每次获取的条数
        Integer queryTimeOut = 100000; // 查询超时时间
        Integer dereplication = 0; // 去重
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
