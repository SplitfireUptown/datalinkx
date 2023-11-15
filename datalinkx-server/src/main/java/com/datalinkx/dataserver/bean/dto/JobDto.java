package com.datalinkx.dataserver.bean.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class JobDto {

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class StatusRefresh {
        private String jobId;
        private Integer status;
    }

    @Data
    @ApiModel
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class DataCountDto {
        private Long appendCount;
        private Long updateCount;
        private Long deleteCount;
        private Long failedCount;
        private Long allCount;
    }
}
