<template>
  <div class="system-monitor">
    <a-card title="系统状态监控" :bordered="false">
      <LoadingDx size="'size-1x'" v-if="selectloading"></LoadingDx>

      <a-row :gutter="24">
        <a-col :span="8">
          <a-card class="metric-card">
            <a-statistic
              title="CPU使用率"
              :value="systemStatus.cpuUsage"
              :precision="2"
              suffix="%"
            >
              <template #prefix>
                <a-icon type="dashboard" />
              </template>
            </a-statistic>
            <a-progress :percent="parseFloat(systemStatus.cpuUsage)" :showInfo="false" status="active"/>
          </a-card>
        </a-col>

        <a-col :span="8">
          <a-card class="metric-card">
            <a-statistic
              title="内存使用率"
              :value="systemStatus.memoryRate"
              :precision="2"
              suffix="%"
            >
              <template #prefix>
                <a-icon type="database" />
              </template>
            </a-statistic>
            <a-progress :percent="parseFloat(systemStatus.memoryRate)" :showInfo="false" status="active"/>
          </a-card>
        </a-col>

        <a-col :span="8">
          <a-card class="metric-card">
            <a-statistic
              title="磁盘使用率"
              :value="calculateDiskUsage"
              :precision="2"
              suffix="%"
            >
              <template #prefix>
                <a-icon type="hdd" />
              </template>
            </a-statistic>
            <a-progress :percent="calculateDiskUsage" :showInfo="false" status="active"/>
          </a-card>
        </a-col>
      </a-row>

      <a-divider />

      <a-row :gutter="24">
        <a-col :span="12">
          <a-card title="系统信息" :bordered="false" class="info-card">
            <a-descriptions :column="1">
              <a-descriptions-item label="操作系统">
                <a-icon type="windows" v-if="systemStatus.systemOs.includes('Windows')" />
                <a-icon type="apple" v-else-if="systemStatus.systemOs.includes('Mac')" />
                <a-icon type="ubuntu" v-else />
                {{ systemStatus.systemOs }}
              </a-descriptions-item>
              <a-descriptions-item label="CPU总数">
                <a-icon type="cluster" /> {{ systemStatus.cpus }} 核
              </a-descriptions-item>
              <a-descriptions-item label="进程ID">
                <a-icon type="number" /> {{ systemStatus.pid }}
              </a-descriptions-item>
              <a-descriptions-item label="进程启动时间">
                <a-icon type="clock-circle" /> {{ systemStatus.programRunTime }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>

        <a-col :span="12">
          <a-card title="JVM信息" :bordered="false" class="info-card">
            <a-descriptions :column="1">
              <a-descriptions-item label="JDK版本">
                <a-icon type="code" /> {{ systemStatus.javaVersion }}
              </a-descriptions-item>
              <a-descriptions-item label="JVM内存大小">
                <a-icon type="fund" /> {{ systemStatus.jvmMemorySize }}
              </a-descriptions-item>
              <a-descriptions-item label="JVM内存已使用">
                <a-icon type="deployment-unit" /> {{ systemStatus.jvmMemoryUsed }}
              </a-descriptions-item>
              <a-descriptions-item label="机器总内存">
                <a-icon type="cloud-server" /> {{ systemStatus.machineMemorySize }}
              </a-descriptions-item>
            </a-descriptions>
          </a-card>
        </a-col>
      </a-row>

    </a-card>
  </div>
</template>

<script>
import { Card, Row, Col, Statistic, Progress, Descriptions, Divider, Icon } from 'ant-design-vue'
import { getSystemMonitor } from '@/api/system/monitor'
import LoadingDx from '@/components/common/loading-dx.vue'

export default {
  components: {
    LoadingDx,
    'a-card': Card,
    'a-row': Row,
    'a-col': Col,
    'a-statistic': Statistic,
    'a-progress': Progress,
    'a-descriptions': Descriptions,
    'a-descriptions-item': Descriptions.Item,
    'a-divider': Divider,
    'a-icon': Icon
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
  computed: {
    calculateDiskUsage() {
      const total = parseFloat(this.systemStatus.totalDisk)
      const free = parseFloat(this.systemStatus.freeDisk)
      if(total && free) {
        return Number(((total - free) / total * 100).toFixed(2))
      }
      return 0
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

<style scoped>
.system-monitor {
  padding: 24px;
  background: #f0f2f5;
}

.metric-card {
  margin-bottom: 24px;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.07);
}

.info-card {
  height: 100%;
  border-radius: 4px;
  box-shadow: 0 1px 2px rgba(0,0,0,0.07);
}

.ant-statistic-title {
  margin-bottom: 8px;
  color: rgba(0,0,0,0.45);
}

.ant-statistic-content {
  color: rgba(0,0,0,0.85);
  font-size: 24px;
  margin-bottom: 16px;
}

.ant-descriptions-item-label {
  color: rgba(0,0,0,0.65);
}

.ant-descriptions-item-content {
  color: rgba(0,0,0,0.85);
}

.ant-card-head {
  border-bottom: 1px solid #f0f0f0;
  padding: 0 24px;
  min-height: 48px;
}
</style>
