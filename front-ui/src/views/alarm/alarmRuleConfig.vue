<template>
  <a-modal
    title="告警规则配置"
    :width="640"
    :visible="visible"
    :confirmLoading="confirmLoading"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <a-spin :spinning="confirmLoading">
      <a-form :form="form">
        <a-form-item
          v-show="false"
          label="ds_id"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input
            v-model="ruleId"
            :disabled="editable || showable"
          />
        </a-form-item>
        <a-form-item
          label="名称"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input
            type="text"
            :disabled="showable"
            v-model="name" />
        </a-form-item>
        <a-form-item
          label="告警组件"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-select v-model="alarmId" placeholder="请选择告警组件">
            <a-select-option v-for="field in alarmList" :value="field.id" :key="field.id">
              {{ field.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item
          label="流转任务"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-select v-model="jobId" placeholder="请选择流转任务">
            <a-select-option v-for="field in jobList" :value="field.id" :key="field.id">
              {{ field.name }}
            </a-select-option>
          </a-select>
        </a-form-item>

        <a-form-item
          label="规则类型"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-select v-model="ruleType" placeholder="请选择规则类型">
            <a-select-option v-for="field in RuleType" :value="field.value" :key="field.label">
              {{ field.label }}
            </a-select-option>
          </a-select>
        </a-form-item>
      </a-form>
    </a-spin>
    <template slot="footer" >
      <div
        v-show="editable || addable"
      >
        <a-button key="cancel" @click="handleCancel">取消</a-button>
        <a-button key="forward" :loading="confirmLoading" type="primary" @click="handleOk">保存</a-button>
      </div>
    </template>
  </a-modal>

</template>

<script>
import { getObj, addObj, putObj } from '@/api/alarm/alarmRule'
import { listQuery } from '@/api/alarm/alarm'
import { listQuery as listJobQuery } from '@/api/job/job'
let selectTables = []
const RuleType = [
  {
    label: '忽略状态推送',
    value: 0
  },
  {
    label: '失败推送',
    value: 1
  },
  {
    label: '成功推送',
    value: 2
  }
]
export default {
  name: 'DsSaveOrUpdate',
  data () {
    return {
      RuleType,
      ruleId: '',
      alarmId: '',
      jobId: '',
      name: '',
      alarmList: [],
      jobList: [],
      ruleType: 0,
      labelCol: {
        xs: { span: 24 },
        sm: { span: 7 }
      },
      wrapperCol: {
        xs: { span: 24 },
        sm: { span: 13 }
      },
      longWrapperCol: {
        xs: { span: 44 },
        sm: { span: 13 }
      },
      visible: false,
      confirmLoading: false,
      form: this.$form.createForm(this),
      editable: false,
      addable: false,
      showable: false,
      type: 'add'
    }
  },
  created () {
    this.init()
  },
  methods: {
    // 获取用户信息
    edit (id, type) {
      this.visible = true
      if (type && type === 'add') {
        this.addable = true
        this.type = type
      }
      if (type === 'edit') {
        this.editable = true
        this.type = type
      }
      if (type === 'show') {
        this.showable = true
        this.type = type
      }

      if (['edit', 'show'].includes(type)) {
        this.confirmLoading = true
        getObj(id).then(res => {
          this.ruleId = res.result.rule_id
          this.name = res.result.rule_name
          console.log(res.result.rule_name)
          this.alarmId = res.result.alarm_id
          this.jobId = res.result.job_id
          this.ruleType = res.result.type
          this.confirmLoading = false
        })
      } else {
        this.ruleId = ''
        this.name = ''
        this.alarmId = ''
        this.jobId = ''
        this.ruleType = ''
      }
    },
    handleChange (value) {
      selectTables.push(value)
      console.log(`Selected: ${value}`)
      console.log(selectTables)
    },
    // 结束保存用户信息
    handleOk () {
      const config = {
        'name': this.name,
        'alarm_id': this.alarmId,
        'job_id': this.jobId,
        'type': this.ruleType,
        'status': 1
      }
      this.confirmLoading = true
      if (this.type === 'add') {
        addObj(config).then(res => {
          if (res.status === '0') {
            this.$emit('ok')
            this.confirmLoading = false
            // 清楚表单数据
            this.handleCancel()
            this.$message.success('新增成功')
          } else {
            this.confirmLoading = false
            this.$message.error(res.errstr)
          }
        }).catch(err => {
          this.confirmLoading = false
          this.$message.error(err.errstr)
        })
      } else if (this.type === 'edit') {
        config['rule_id'] = this.ruleId
        putObj(config).then(res => {
          if (res.status === '0') {
            this.$emit('ok')
            this.confirmLoading = false
            // 清楚表单数据
            this.handleCancel()
            this.$message.success('更新成功')
          } else {
            this.confirmLoading = false
            this.$message.error(res.errstr)
          }
        }).catch(err => {
          this.confirmLoading = false
          this.$message.error(err.errstr)
        })
      }
      selectTables = []
    },
    handleCancel () {
      this.visible = false
      this.addable = false
      this.showable = false
      this.editable = false
      this.name = ''
      this.alarmId = ''
      this.jobId = ''
    },
    init () {
      this.loading = true
      listQuery({
        ...this.queryParam,
        ...this.pages
      }).then(res => {
        for (var a of res.result) {
          this.alarmList.push({
            'id': a.alarm_id,
            'name': a.name
          })
        }
      })

      listJobQuery().then(res => {
        for (const i of res.result) {
          if (i.type !== 1) {
            this.jobList.push({
              'name': i.job_name,
              'id': i.job_id
            })
          }
        }
      }).finally(() => {
        this.loading = false
      })
      console.log(this.alarmList)
    },
    handleSearch (value) {

    }
  }
}
</script>

<style scoped>

</style>
