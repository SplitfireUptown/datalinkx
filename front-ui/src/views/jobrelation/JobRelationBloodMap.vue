<template>
  <div>
    <div class="data-lineage-graph">
      <h1 class="header">任务血缘关系图</h1>
      <div class="task-select">
        <b>任务选择：</b>
        <a-select v-model="selectedTask" @change="handleTaskChange" style="width: 400px;">
          <a-select-option v-for="task in taskList" :value="task.value" :key="task.label">{{ task.label }}</a-select-option>
        </a-select>
      </div>
      <div ref="graphContainer" class="graph-container"></div>
    </div>
  </div>
</template>

<script>
import { Network } from 'vis-network/standalone/esm/vis-network'
import { EventEmitter } from 'events'
import { listQuery } from '@/api/job/job'
import { relationInfo } from '@/api/job/jobrelation'
EventEmitter.defaultMaxListeners = 0 // 根据你的需要设置适当的数量

export default {
  data () {
    return {
      selectedTask: '', // 存储选中的任务
      taskList: [], // 任务列表
      graphData: {
        nodes: [
          // { id: 1, label: '数据源 A' },
          // { id: 2, label: '数据源 B' },
          // { id: 3, label: '处理步骤 1' },
          // { id: 4, label: '处理步骤 2' },
          // { id: 5, label: '结果数据' }
        ],
        edges: [
          // { from: 1, to: 3 },
          // { from: 2, to: 3 },
          // { from: 3, to: 4 },
          // { from: 4, to: 5 }
        ]
      },
      network: null
    }
  },
  mounted () {
    this.initializeGraph()
  },
  methods: {
    initializeGraph () {
      const container = this.$refs.graphContainer
      const options = {
        nodes: {
          shape: 'box'
        },
        edges: {
          arrows: 'to'
        }
      }
      this.network = new Network(container, this.graphData, options)
    },
    handleTaskChange () {
      // 处理任务变更
      relationInfo(this.selectedTask).then(res => {
        this.graphData.nodes = res.result.nodes
        this.graphData.edges = res.result.edges
        this.initializeGraph()
      })
    },
    fetchTaskList () {
      listQuery().then(res => {
          for (const i of res.result) {
            if (i.type !== 1) {
              this.taskList.push({
                'label': i.job_name,
                'value': i.job_id
              })
            }
            this.loading = false
          }
        }
      )
    }
  },
  created () {
    this.fetchTaskList()
  }
}
</script>

<style scoped>
.data-lineage-graph {
  max-width: 1200px;
  margin: 0 auto;
}

.header {
  text-align: center;
  color: #1890ff;
  font-size: 24px;
  padding: 20px 0;
}

.graph-container {
  height: 600px;
  border: 1px solid #ccc
}

.task-select {
  margin-bottom: 20px;
}
</style>
