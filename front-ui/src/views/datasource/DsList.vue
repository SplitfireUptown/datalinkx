<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="数据源名称">
              <a-input v-model="queryParam.name" placeholder="数据源名称"/>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-button @click="() => {this.queryData()}" type="primary">查询</a-button>
            <a-button @click="() => queryParam = {}" style="margin-left: 8px">重置</a-button>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <div class="table-operator">
      <!--      v-action="'upms:user:add'"-->
      <a-button @click="$refs.DsSaveOrUpdate.edit('','add')" icon="plus" type="primary">新建</a-button>
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
    <ds-save-or-update
      @ok="handleOk"
      ref="DsSaveOrUpdate"
    />
  </a-card>
</template>

<script>
import { pageQuery, delObj } from '@/api/datasource/datasource'
import DsSaveOrUpdate from './DsSaveOrUpdate.vue'
const DataSourceType = [
  {
    label: 'MySQL',
    value: 1
  },
  {
    label: 'ELASTICSEARCH',
    value: 2
  }
]
export default {
  name: 'ContainerBottom',
  components: {
    DsSaveOrUpdate
  },
  data () {
    return {
      loading: false,
      columns: [
        {
          title: 'ds_id',
          width: '10%',
          dataIndex: 'dsId'
        }, {
          title: '数据源名称',
          width: '10%',
          dataIndex: 'name'
        },
        {
          title: '数据源类型',
          width: '10%',
          dataIndex: 'type',
          customRender: (text) => {
            return (
              <div>
                {DataSourceType.map(item => {
                  if (item.value === text) {
                    return <span>{item.label}</span>
                  }
                })}
              </div>
            )
          }
        },
        {
          title: '创建时间',
          width: '10%',
          dataIndex: 'ctime',
          sorter: true
        }, {
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
                <a href="javascript:;"onClick={(e) => this.show(record)}>查看</a>
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
      }
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
        console.log(res)
        this.pagination.total = +res.result.total
        this.loading = false
      }).catch(reason => {
        this.loading = false
      })
    },
    handleTableChange (pagination, filters, sorter) {
      console.log(sorter.field)
      console.log(sorter.order)
      this.pagination = pagination
      this.pages.size = pagination.pageSize
      this.pages.current = pagination.current
      this.init()
    },

    edit (record) {
      this.$refs.DsSaveOrUpdate.edit(record.dsId, 'edit')
      this.init()
    },
    delete (record) {
      console.log(record)
      delObj(record.dsId).then(res => {
        this.$message.info('删除成功')
        this.init()
      })
    },
    show (record) {
      this.$refs.DsSaveOrUpdate.edit(record.dsId, 'show')
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
