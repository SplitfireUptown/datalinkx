<template>
  <a-card :bordered="false">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" style="text-align: left;">
            <a-dropdown :placement="placement">
              <a-button icon="plus" type="primary">新建</a-button>
              <a-menu slot="overlay">
                <a-menu-item>
                  <a-button @click="$refs.AlarmEmailConfig.edit('', 'add')">新建邮箱配置</a-button>
                </a-menu-item>
                <a-menu-item>
                  <a-button @click="$refs.AlarmDingTalkConfig.edit('', 'add')">新建钉钉配置</a-button>
                </a-menu-item>
              </a-menu>
            </a-dropdown>
            <!--            <a-button @click="add()" icon="plus" type="primary">新建</a-button>-->
          </a-col>
        </a-row>
      </a-form>
    </div>
    <br/>
    <a-table
      :columns="columns"
      :dataSource="tableData"
      :loading="loading"
      :rowKey="record => record.id"
    >
    </a-table>
    <alarm-email-config
      @ok="handleOk"
      ref="AlarmEmailConfig"
    ></alarm-email-config>
    <alarm-ding-talk-config
      @ok="handleOk"
      ref="AlarmDingTalkConfig"
    ></alarm-ding-talk-config>
  </a-card>
</template>

<script>
import { listQuery, delObj } from '@/api/alarm/alarm'
import AlarmEmailConfig from '../alarm/alarmEmailConfig.vue'
import AlarmDingTalkConfig from '../alarm/alarmDingTalkConfig.vue'

const AlarmType = [
  {
    label: '邮箱',
    value: 0
  },
  {
    label: '钉钉',
    value: 1
  }
]
export default {
  name: 'AlarmConfig',
  components: {
    AlarmEmailConfig,
    AlarmDingTalkConfig
  },
  data () {
    return {
      loading: false,
      columns: [
        {
          title: 'alarm_id',
          dataIndex: 'alarm_id'
        },
        {
          title: '报警类型',
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
          title: '名称',
          dataIndex: 'name'
        },
        {
          title: '操作',
          customRender: (record) => {
            return (
              <div>
              <a onClick={(e) => this.edit(record)}>修改</a>
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
      tableData: []
    }
  },
  methods: {
    init () {
      this.loading = true
      listQuery({
        ...this.queryParam,
        ...this.pages
      }).then(res => {
        this.tableData = res.result
      }).finally(() => {
        this.loading = false
      })
    },
    delete (record) {
      delObj(record.alarm_id).then(res => {
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
    edit (record) {
      if (record.type === 1) {
        this.$refs.AlarmDingTalkConfig.edit(record.alarm_id, 'edit')
      } else if (record.type === 0) {
        this.$refs.AlarmEmailConfig.edit(record.alarm_id, 'edit')
      }
    },
    add () {
      this.$refs.AlarmEmailConfig.edit('', 'add')
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
