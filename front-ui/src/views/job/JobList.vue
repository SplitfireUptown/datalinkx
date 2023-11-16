<template>
  <a-card :bordered="false">
    <!--    <div class="table-page-search-wrapper">-->
    <!--      <a-form layout="inline">-->
    <!--        <a-row :gutter="48">-->
    <!--          <a-col :md="8" :sm="24">-->
    <!--            <a-form-item label="数据源名称">-->
    <!--              <a-input v-model="queryParam.name" placeholder="数据源名称"/>-->
    <!--            </a-form-item>-->
    <!--          </a-col>-->
    <!--          <a-col :md="8" :sm="24">-->
    <!--            <a-button @click="() => {this.queryData()}" type="primary">查询</a-button>-->
    <!--            <a-button @click="() => queryParam = {}" style="margin-left: 8px">重置</a-button>-->
    <!--          </a-col>-->
    <!--        </a-row>-->
    <!--      </a-form>-->
    <!--    </div>-->
    <div class="table-operator">
      <a-button @click="$refs.JobSaveOrUpdate.edit('add', '')" icon="plus" type="primary">新建</a-button>
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
    <job-save-or-update
      @ok="handleOk"
      ref="JobSaveOrUpdate"
    />
  </a-card>
</template>

<script>
import { pageQuery, delObj, exec } from '@/api/job/job'
import { closeConnect } from '@/api/job/sse'
import JobSaveOrUpdate from '../job/JobSaveOrUpdate.vue'
// 0:CREATE|1:SYNCING|2:SYNC_FINISH|3:SYNC_ERROR|4:QUEUING
const StatusType = [
  {
    label: '新建',
    value: 0
  },
  {
    label: '同步中',
    value: 1
  },
  {
    label: '同步完成',
    value: 2
  },
  {
    label: '同步失败',
    value: 3
  },
  {
    label: '排队中',
    value: 4
  }
]
export default {
  name: 'JobList',
  components: {
    JobSaveOrUpdate
  },
  data () {
    return {
      loading: false,
      columns: [
        {
          title: 'job_id',
          width: '10%',
          dataIndex: 'job_id'
        }, {
          title: '来源表',
          width: '10%',
          dataIndex: 'from_tb_name'
        },
        {
          title: '目标表',
          width: '10%',
          dataIndex: 'to_tb_name',
          sorter: true
        },
        {
          title: '任务状态',
          width: '10%',
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
          title: '同步进度',
          width: '10%',
          dataIndex: 'progress'
        },
        {
          title: '操作',
          width: '15%',
          customRender: (record) => {
            return (
              <div>
                <a onClick={(e) => this.edit(record)}>修改</a>
                <a-divider type="vertical" />
                <a-popconfirm title="是否删除" onConfirm={() => this.delete(record)} okText="是" cancelText="否">
                  <a-icon slot="icon" type="question-circle-o" style="color: red" />
                  <a href="javascript:;" style="color: red">删除</a>
                </a-popconfirm>
                <a-divider type="vertical" />
                <a href="javascript:;"onClick={(e) => this.execJob(record)}>手动触发</a>
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
        this.tableData = res.data
        this.pagination.total = +res.total
        this.loading = false
      }).finally(() => {
        this.loading = false
      })
    },
    handleTableChange (pagination, filters, sorter) {
      this.pagination = pagination
      this.pages.size = pagination.pageSize
      this.pages.current = pagination.current
      this.init()
    },

    edit (record) {
      this.$refs.JobSaveOrUpdate.edit('edit', record.job_id)
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
    show (record) {
      this.$refs.JobSaveOrUpdate.edit(record.job_id, 'show')
    },
    execJob (record) {
      exec(record.job_id).then(res => {
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
    handleOk () {
      this.init()
    },
    queryData () {
      this.pages.current = 1
      this.init()
    },
    createEventSource () {
      if (window.EventSource) {
        this.eventSource = new EventSource(
          `api/api/sse/connect/jobList`, {
            // 设置重连时间
            heartbeatTimeout: 60 * 60 * 1000
            // 添加token
          })
        this.eventSource.onopen = (e) => {
          console.log('connect success')
        }
        this.eventSource.onmessage = (e) => {
          // console.log('from server data:', e.data)
          this.init()
        }
      } else {
        console.log('browser not support SSE')
      }
    }
  },
  beforeDestroy () {
    this.eventSource.close()
    closeConnect('jobList')
  },
  created () {
    this.init()
    this.createEventSource()
  }
}
</script>

<style scoped>

</style>
