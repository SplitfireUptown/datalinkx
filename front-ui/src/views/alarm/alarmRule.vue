<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" style="text-align: left;">
            <a-button @click="$refs.AlarmRuleConfig.edit('', 'add')" icon="plus" type="primary">新建</a-button>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <br/>
    <a-table
      :columns="columns"
      :dataSource="tableData"
      :loading="loading"
      :pagination="pagination"
      :rowKey="record => record.job_id"
      @change="handleTableChange"
    >
    </a-table>
    <alarm-rule-config
      @ok="handleOk"
      ref="AlarmRuleConfig"
    ></alarm-rule-config>
  </a-card>
</template>

<script>
import { listRuleQuery, shutdownObj, delRuleObj } from '@/api/alarm/alarm'
import AlarmRuleConfig from '../alarm/alarmRuleConfig.vue'

const StatusType = [
  {
    label: '禁用',
    value: 0
  },
  {
    label: '开启',
    value: 1
  }
]
const AlarmType = [
  {
    label: '结束后推送',
    value: 0
  },
  {
    label: '失败后推送',
    value: 1
  },
  {
    label: '成功后推送',
    value: 2
  }
]
export default {
  name: 'AlarmConfig',
  components: {
    AlarmRuleConfig
  },
  data () {
    return {
      loading: false,
      columns: [
        {
          title: 'ruleId',
          dataIndex: 'rule_id'
        },
        {
          title: '告警规则名称',
          dataIndex: 'rule_name'
        },
        {
          title: '告警组件名称',
          dataIndex: 'alarm_component_name'
        },
        {
          title: '依赖任务名称',
          dataIndex: 'job_name'
        },
        {
          title: '最后一次推送时间',
          dataIndex: 'push_time'
        },
        {
          title: '状态',
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
          title: '告警类型',
          dataIndex: 'type',
          customRender: (text) => {
            return (
              <div>
                {AlarmType.map(item => {
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
          customRender: (record) => { 
            const isEnabled = record.status === 1; 
            return ( 
              <div> 
                <a onClick={(e) => this.edit(record)}>修改</a> 
                <a-divider type="vertical" /> 
                <a href="javascript:;" onClick={(e) => this.shutdownJob(record)}> 
                  {isEnabled ? '停用' : '开启'} 
                </a> 
                <a-divider type="vertical" /> 
                <a-popconfirm title="是否删除" onConfirm={() => this.delete(record)} okText="是" cancelText="否"> 
                  <a-icon slot="icon" type="question-circle-o" style="color: red" /> 
                  <a href="javascript:;" style="color: red">删除</a> 
                </a-popconfirm> 
              </div> 
            ); 
          } 
        }
      ],
      queryParam: {
        'type': 0
      },
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
      tableData: []
    }
  },
  methods: {
    init () {
      this.loading = true
      listRuleQuery({
        ...this.queryParam,
        ...this.pages
      }).then(res => {
        this.tableData = res.result
      }).finally(() => {
        this.loading = false
      })
    },
    shutdownJob (record) {
      shutdownObj(record.rule_id).then(res => {
        if (res.status === '0') {
          this.$message.info('停用成功')
          this.init()
        } else {
          this.$message.error(res.errstr)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    delete (record) {
      delRuleObj(record.rule_id).then(res => {
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
    handleTableChange (pagination) {
      this.pagination = pagination
      this.pages.size = pagination.pageSize
      this.pages.current = pagination.current
      this.init()
    },
    edit (record) {
      this.$refs.AlarmRuleConfig.edit(record.rule_id, 'edit')
    },
    handleOk () {
      this.init()
    },
    mounted () {
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
