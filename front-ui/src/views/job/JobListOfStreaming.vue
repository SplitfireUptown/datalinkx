<template>
  <a-card :bordered="false">
    <div class="table-operator">
      <a-button @click="$refs.JobSaveOrUpdateStreaming.edit('add', '', 'streaming')" icon="plus" type="primary">新建</a-button>
    </div>
    <a-table
      :columns="columns"
      :dataSource="tableData"
      :loading="loading"
      :pagination="pagination"
      :rowKey="record => record.id"
      @change="handleTableChange"
    >
    </a-table>
    <job-save-or-update-streaming
      @ok="handleOk"
      ref="JobSaveOrUpdateStreaming"
    />
  </a-card>
</template>

<script>
import { pageQuery, delObj, streamStop, streamExec } from '@/api/job/job'
import JobSaveOrUpdateStreaming from '@/views/job/JobSaveOrUpdateStreaming.vue'
// 0:CREATE|1:SYNCING|2:SYNC_FINISH|3:SYNC_ERROR|4:QUEUING
const StatusType = [
  {
    label: '新建',
    value: 0
  },
  {
    label: '流转中',
    value: 1
  },
  {
    label: '流转停止',
    value: 2
  },
  {
    label: '流转失败',
    value: 3
  },
  {
    label: '流转停止',
    value: 4
  }
]
export default {
  name: 'JobListOfStreaming',
  components: {
    JobSaveOrUpdateStreaming
  },
  data () {
    return {
      loading: false,
      columns: [
        {
          title: 'job_id',
          // width: '30%',
          dataIndex: 'job_id'
        },
        {
          title: '任务名称',
          dataIndex: 'job_name'
        },
        {
          title: '来源表',
          // width: '10%',
          dataIndex: 'from_tb_name'
        },
        {
          title: '目标表',
          // width: '10%',
          dataIndex: 'to_tb_name'
        },
        {
          title: '任务状态',
          // width: '30%',
          dataIndex: 'status',
          customRender: (text) => {
            return (
              <div>
                {StatusType.map(item => {
                  if (item.value === text) {
                    return <span>{item.label}</span>
                  }
                })}
              </div>
            )
          }
        },
        {
          title: '操作',
          // width: '20%',
          customRender: (record) => {
            return (
              <div>
                <a href="javascript:;"onClick={(e) => this.stopJob(record)}>停止</a>
                <a-divider type="vertical" />
                <a href="javascript:;"onClick={(e) => this.execJob(record)}>触发</a>
                <a-divider type="vertical" />
                <a-popconfirm title="是否删除" onConfirm={() => this.delete(record)} okText="是" cancelText="否">
                  <a-icon slot="icon" type="question-circle-o" style="color: red" />
                  <a href="javascript:;" style="color: red">删除</a>
                </a-popconfirm>
              </div>
            )
          }
        }
      ],
      tableData: [],
      pagination: {
        pageSize: 10,
        current: 1,
        total: 0,
        showSizeChanger: true
      },
      pages: {
        size: 10,
        current: 1
      },
      queryParam: {
        'type': 1
      },
      source: null
    }
  },
  methods: {
    init () {
      this.loading = true
      pageQuery({
        ...this.queryParam,
        ...this.pages
      }).then(res => {
        this.tableData = res.result.data
        this.pagination.total = +res.result.total
        this.loading = false
      }).finally(() => {
        this.loading = false
      })
    },
    handleTableChange (pagination) {
      this.pagination = pagination
      this.pages.size = pagination.pageSize
      this.pages.current = pagination.current
      this.init()
    },

    edit (record) {
      this.$refs.JobSaveOrUpdateStreaming.edit('edit', record.job_id)
    },
    delete (record) {
      delObj(record.job_id).then(res => {
        if (res.status === '0') {
          this.$message.info('删除成功')
          this.init()
        } else {
          this.$message.error(res.errstr)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    execJob (record) {
      streamExec(record.job_id).then(res => {
        if (res.status === '0') {
          this.$message.info('触发成功')
          this.init()
        } else {
          this.$message.error(res.errstr)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    show (record) {
      this.$refs.JobSaveOrUpdateStreaming.edit(record.job_id, 'show')
    },
    stopJob (record) {
      streamStop(record.job_id).then(res => {
        this.loading = false
        if (res.status === '0') {
          this.$message.info('停止成功')
          this.init()
        } else {
          this.$message.error(res.errstr)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    handleOk () {
      this.init()
    },
    queryData () {
      this.pages.current = 1
      this.init()
    }
  },
  created () {
    this.init()
  }
}
</script>

<style scoped>

</style>
