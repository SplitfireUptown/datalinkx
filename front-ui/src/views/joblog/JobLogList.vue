<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="任务id">
              <a-input v-model="queryParam.jobId" placeholder="任务id"/>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-button @click="() => {this.queryData()}" type="primary">查询</a-button>
            <a-button @click="() => queryParam = {}" style="margin-left: 8px">重置</a-button>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <a-table
      :columns="columns"
      :dataSource="tableData"
      :pagination="pagination"
      :rowKey="record => record.id"
      @change="handleTableChange"
    >
      <span slot="status" slot-scope="text, record" style="display: flex;align-items: center;">
        <div :style="getTableCss(record.status)"></div>
        <span>{{ record.status ? '失败' : '成功' }}</span>
      </span>
    </a-table>
  </a-card>
</template>

<script>
import { pageQuery } from '@/api/job/joblog'
// 0:CREATE|1:SYNCING|2:SYNC_FINISH|3:SYNC_ERROR|4:QUEUING
export default {
  name: 'ContainerBottom',
  components: {
    // JobSaveOrUpdate
  },
  data () {
    return {
      loading: false,
      columns: [
        {
          title: 'job_id',
          width: '10%',
          dataIndex: 'job_id'
        },
        {
          title: '执行日志',
          width: '10%',
          dataIndex: 'error_msg',
          ellipsis: {
            showTitle: false
          },
          customRender: (value) => {
            return this.$createElement('a-tooltip', {
              props: {
                title: value || ''
              }
            }, [
              this.$createElement('div', {
                class: 'ellipsis',
                style: {
                  float: 'left',
                  maxWidth: '100%'
                },
                domProps: {
                  textContent: value ? value.substring(0, 30) : ''
                }
              })
            ])
          }
        },
        {
          title: '开始时间',
          width: '10%',
          dataIndex: 'start_time',
          sorter: true
        },
        {
          title: '执行耗时s',
          width: '10%',
          dataIndex: 'cost_time',
          sorter: true
        },
        {
          title: '任务状态',
          width: '10%',
          dataIndex: 'status',
          scopedSlots: { customRender: 'status' }
        },
        {
          title: '新增条数',
          width: '10%',
          dataIndex: 'append_count',
          sorter: true
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
        page_size: 10,
        page_no: 1
      },
      queryParam: {
        jobId: ''
      }
    }
  },
  methods: {
    init () {
      this.loading = true
      pageQuery({
        ...this.pages,
        ...this.queryParam
      }).then(res => {
        this.tableData = res.result.data
        this.pagination.total = +res.result.total
        this.loading = false
      }).finally(() => {
        this.loading = false
        this.queryParam.jobId = ''
      })
    },
    getTableCss (status) {
      return `width:12px;height:12px;border-radius:50%;margin-right:4px;background-color: ${status ? '#f5222d' : '#52c41a'};`
    },
    handleTableChange (pagination, filters, sorter) {
      this.pagination = pagination
      this.pages.page_size = pagination.pageSize
      this.pages.page_no = pagination.current
      this.init()
    },
    handleOk () {
      this.init()
    },
    queryData () {
      this.pages.page_no = 1
      this.init()
    }
  },
  created () {
    this.init()
  }
}
</script>

<style scoped lang="less">
</style>
