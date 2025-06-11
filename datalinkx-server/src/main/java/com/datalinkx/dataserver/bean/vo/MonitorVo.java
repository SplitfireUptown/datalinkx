package com.datalinkx.dataserver.bean.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

public class MonitorVo {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SystemMonitorVo {
        // 总磁盘
        private String totalDisk;
        // 剩余磁盘
        private String freeDisk;
        // 操作系统
        private String systemOs;
        // 程序运行时间
        private String programRunTime;
        // 进程id
        private String pid;
        // cpu 核数
        private String cpus;
        // cpu使用率
        private String cpuUsage;
        // java home
        private String javaVersion;
        // jvm堆内存大小
        private String jvmMemorySize;
        // jvm使用堆内存
        private String jvmMemoryUsed;
        // 内存使用率
        private String memoryRate;
        // 总机器内存
        private String machineMemorySize;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssetTotalMonitorVo {
        private Long totalDs;
        private Long todayIncreaseDs;
        private Long totalJob;
        private Long todayIncreaseJob;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AssetDsJobGroupVo {
        @JsonProperty("job_run_count")
        private List<ItemCountVo> jobRunCount;
        @JsonProperty("job_type_count")
        private List<NameTotalVo> jobTypeCount;
    }


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemCountVo {
        private String item;
        private Long count;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NameTotalVo {
        private String name;
        private Long total;
    }
}
