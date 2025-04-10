package com.datalinkx.dataserver.monitor;

import com.datalinkx.dataserver.bean.vo.SystemMonitorVo;
import com.sun.management.OperatingSystemMXBean;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * 系统监控
 */
public class SystemMonitor {

    public static SystemMonitorVo stat() {
        SystemMonitorVo result = new SystemMonitorVo();

        OperatingSystemMXBean osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        // 椎内存使用情况
        MemoryUsage memoryUsage = memoryMXBean.getHeapMemoryUsage();

        // 初始的总内存
        long initTotalMemorySize = memoryUsage.getInit();
        // 已使用的内存
        long usedMemorySize = memoryUsage.getUsed();

        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory(); // 总内存
        long freeMemory = runtime.freeMemory(); // 可用内存
        long usedMemory = totalMemory - freeMemory; // 已使用内存
        long maxMemory = runtime.maxMemory(); // 最大可用内存
        double useMemoryRate = (double) usedMemory / maxMemory * 100;

        // 操作系统
        String osName = System.getProperty("os.name");
        // 总的物理内存
        String totalMemorySize = new DecimalFormat("#.##")
                .format(osmxb.getTotalPhysicalMemorySize() / 1024.0 / 1024 / 1024) + "G";
        // 磁盘使用情况
        File[] files = File.listRoots();
        for (File file : files) {
            String total = new DecimalFormat("#.#").format(file.getTotalSpace() * 1.0 / 1024 / 1024 / 1024) + "G";
            String un = new DecimalFormat("#.#").format(file.getUsableSpace() * 1.0 / 1024 / 1024 / 1024) + "G";
            result.setTotalDisk(total);
            result.setFreeDisk(un);
        }

        // 获取当前JVM进程的CPU时间（以毫秒为单位）
        long processCpuTime = osmxb.getProcessCpuTime();
        // 等待一段时间以获取新的读数
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        osmxb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        long processCpuTime2 = osmxb.getProcessCpuTime();
        // 计算CPU使用率
        double cpuUsage = (processCpuTime2 - processCpuTime) / (1000000.0 /* 转换为秒 */ * osmxb.getAvailableProcessors());

        result.setCpuUsage(String.format("%.2f", cpuUsage * 100));
        result.setSystemOs(osName);
        result.setProgramRunTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(ManagementFactory.getRuntimeMXBean().getStartTime())));
        result.setPid(System.getProperty("PID"));
        result.setCpus(String.valueOf(Runtime.getRuntime().availableProcessors()));
        result.setJavaVersion(System.getProperty("java.version"));
        result.setJvmMemorySize(new DecimalFormat("#.#").format(initTotalMemorySize * 1.0 / 1024 / 1024) + "M");
        result.setJvmMemoryUsed(new DecimalFormat("#.#").format(usedMemorySize * 1.0 / 1024 / 1024) + "M");
        result.setMachineMemorySize(totalMemorySize);
        result.setMemoryRate(String.valueOf(useMemoryRate));

        return result;
    }
}
