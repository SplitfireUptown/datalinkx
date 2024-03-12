<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="8">
            <a-form-item label="任务名称">
              <a-input v-model="queryParam.jobName" placeholder="任务名称"/>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="8">
            <a-button @click="() => {this.queryData()}" type="primary">查询</a-button>
            <a-button @click="() => queryParam = {}" style="margin-left: 8px">重置</a-button>
          </a-col>
          <a-col :md="8" :sm="32" style="text-align: right;">
            <a-button @click="$refs.JobRelationSave.add()" icon="plus" type="primary">新建</a-button>
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
    </a-table>
    <job-relation-save
      @ok="handleOk"
      ref="JobRelationSave"
    />
  </a-card>
</template>

<script>
import { pageQuery, delObj } from '@/api/job/jobrelation'
import JobRelationSave from '../jobrelation/JobRelationSave.vue'
export default {
  name: 'JobRelationList',
  components: {
    JobRelationSave
  },
  data () {
    return {
      loading: false,
      columns: [
        {
          title: 'relation_id',
          width: '10%',
          dataIndex: 'relation_id'
        },
        {
          title: '任务名称',
          width: '10%',
          dataIndex: 'job_name'
        },
        {
          title: '级联子任务名称',
          width: '10%',
          dataIndex: 'sub_job_name'
        },
        {
          title: '任务权重',
          width: '10%',
          dataIndex: 'priority'
        },
        {
          title: '操作',
          width: '15%',
          customRender: (record) => {
            return (
              <div>
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
        page_size: 10,
        page_no: 1
      },
      queryParam: {
        jobName: ''
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
        this.queryParam.jobName = ''
      })
    },
    delete (record) {
      delObj(record.relation_id).then(res => {
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
