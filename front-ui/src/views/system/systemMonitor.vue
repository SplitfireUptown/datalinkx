<template>
  <a-card title="系统状态">
    <LoadingDx size="'size-1x'" v-if="selectloading"></LoadingDx>
    <a-descriptions column="1">
      <a-descriptions-item label="磁盘总量">{{ systemStatus.totalDisk }}</a-descriptions-item>
      <a-descriptions-item label="磁盘空闲量">{{ systemStatus.freeDisk }}</a-descriptions-item>
      <a-descriptions-item label="操作系统">{{ systemStatus.systemOs }}</a-descriptions-item>
      <a-descriptions-item label="进程启动时间">{{ systemStatus.programRunTime }}</a-descriptions-item>
      <a-descriptions-item label="进程ID">{{ systemStatus.pid }}</a-descriptions-item>
      <a-descriptions-item label="CPU总数">{{ systemStatus.cpus }}</a-descriptions-item>
      <a-descriptions-item label="JDK version">{{ systemStatus.javaVersion }}</a-descriptions-item>
      <a-descriptions-item label="JVM内存大小">{{ systemStatus.jvmMemorySize }}</a-descriptions-item>
      <a-descriptions-item label="JVM内存已使用">{{ systemStatus.jvmMemoryUsed }}</a-descriptions-item>
      <a-descriptions-item label="机器总内存">{{ systemStatus.machineMemorySize }}</a-descriptions-item>
      <a-descriptions-item label="CPU使用率">{{ systemStatus.cpuUsage }}</a-descriptions-item>
      <a-descriptions-item label="内存使用率">{{ systemStatus.memoryRate }}</a-descriptions-item>
    </a-descriptions>
  </a-card>
</template>
<script>
import { Card, Table } from 'ant-design-vue'
import { getSystemMonitor } from '@/api/system/monitor'
import LoadingDx from '@/components/common/loading-dx.vue'

export default {
  components: {
    LoadingDx,
    'a-card': Card
  },
  data () {
    return {
      selectloading: false,
      systemStatus: {
        totalDisk: '',
        freeDisk: '',
        systemOs: '',
        programRunTime: '',
        pid: '',
        cpus: '',
        cpuUsage: '',
        javaVersion: '',
        jvmMemorySize: '',
        jvmMemoryUsed: '',
        memoryRate: '',
        machineMemorySize: ''
      }
    }
  },
  created () {
    this.selectloading = true
    getSystemMonitor().then(res => {
      this.systemStatus = res.result
    }).finally(res => {
      this.selectloading = false
    })
  }
}
</script>
