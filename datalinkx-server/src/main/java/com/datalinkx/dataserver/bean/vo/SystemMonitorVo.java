package com.datalinkx.dataserver.bean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemMonitorVo {
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
