<template>
  <a-modal
    title="添加数据源"
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
            v-decorator="['dsId']"
            :disabled="editable || showable"
          />
        </a-form-item>

        <a-form-item
          label="数据源类型"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-select
            :disabled="showable"
            v-decorator="['type', {rules: [{required: true, message: '选择数据源'}]}]"
            :options="DataSourceType"
          >
          </a-select>
        </a-form-item>

        <a-form-item
          label="数据源名称"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input
            type="text"
            :disabled="showable"
            v-decorator="['name', {rules: [{required: true, message: '请输入数据源名称'}]}]" />
        </a-form-item>

        <a-form-item
          label="host"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input
            type="text"
            :disabled="showable"
            v-decorator="['host', {rules: [{required: true, message: '请输入url'}]}]" />
        </a-form-item>

        <a-form-item
          label="port"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input
            type="number"
            :disabled="showable"
            v-decorator="['port', {rules: [{required: true, message: '请输入port'}]}]" />
        </a-form-item>
        <a-form-item
          label="username"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input
            type="text"
            :disabled="showable"
            v-decorator="['username', {rules: [{required: false, message: '请输入username'}]}]" />
        </a-form-item>
        <a-form-item
          label="password"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input
            type="password"
            :disabled="showable"
            v-decorator="['password', {rules: [{required: false, message: '请输入password'}]}]" />
        </a-form-item>
        <a-form-item
          label="database"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input
            type="text"
            :disabled="showable"
            v-decorator="['database', {rules: [{required: false, message: '请输入database'}]}]" />
        </a-form-item>
        <a-form-item
          label="附加配置"
          :labelCol="labelCol"
          :wrapperCol="longWrapperCol"
        >
          <a-textarea
            type="text"
            :disabled="showable"
            v-decorator="['config', {rules: [{required: false, message: '附加配置'}]}]" />
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
import pick from 'lodash.pick'
import { getObj, addObj, putObj } from '@/api/datasource/datasource'
import { DATA_SOURCE_TYPE } from '@/api/globalConstant'
let selectTables = []

export default {
  name: 'DsSaveOrUpdate',
  data () {
    return {
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
      DataSourceType: DATA_SOURCE_TYPE,
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

      const { form: { setFieldsValue, resetFields } } = this
      if (['edit', 'show'].includes(type)) {
        this.confirmLoading = true
        getObj(id).then(res => {
          const record = res.result
          this.confirmLoading = false
          this.$nextTick(() => {
            setFieldsValue(pick(record, ['dsId', 'type', 'name', 'host', 'port', 'username', 'database', 'password', 'config', 'tb_name_list']))
          })
        })
      } else {
        resetFields()
      }
    },
    handleChange (value) {
      selectTables.push(value)
      console.log(`Selected: ${value}`)
      console.log(selectTables)
    },
    // 结束保存用户信息
    handleOk () {
      this.form.validateFields(async (err, values) => {
        if (!err) {
          this.confirmLoading = true
          if (this.type === 'add') {
            console.log(values)
            await addObj(values).then(res => {
              if (res.status !== '0') {
                this.$message.error(res.errstr)
              }
            })
          } else if (this.type === 'edit') {
            await putObj(values)
          }
          setTimeout(() => {
            this.confirmLoading = false
            this.$emit('ok')
            this.visible = false
          }, 1500)
        }
      })
      selectTables = []
    },
    handleCancel () {
      this.visible = false
      setTimeout(() => {
        this.addable = false
        this.showable = false
        this.editable = false
      }, 200)
    },
    init () {
    },
    handleSearch (value) {

    }
  }
}
</script>

<style scoped>

</style>
