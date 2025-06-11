<template>
  <a-modal
    title="添加自定义数据源"
    :width="640"
    :visible="visible"
    :confirmLoading="confirmLoading"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <a-spin :spinning="confirmLoading">
      <a-form :form="form">
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
          :labelCol="labelCol"
          :wrapperCol="longWrapperCol"
        >
          <template slot="label">
            <span>
              附加配置
              <a-tooltip title="请严格使用 JSON 格式书写数据源配置，必须包含自定义数据源类型type，type=插件包中的driver-dist/datalinkx-driver-type-1.0.0.jar 例如：{&quot;type&quot;: &quot;clickhouse&quot;, &quot;username&quot;: &quot;root&quot;, &quot;password&quot;: &quot;123456&quot;}}">
                <a-icon type="question-circle-o" style="margin-left: 4px;" />
              </a-tooltip>
            </span>
          </template>
          <a-textarea
            type="text"
            :autoSize="{ minRows: 10, maxRows: 6 }"
            :disabled="showable"
            v-decorator="['config', {rules: [{required: true, message: '请填写json格式的数据源配置'}]}]" />
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
        xs: { span: 88 },
        sm: { span: 13 }
      },
      visible: false,
      confirmLoading: false,
      form: this.$form.createForm(this),
      editable: false,
      addable: false,
      showable: false,
      dsId: '',
      type: 'add'
    }
  },
  methods: {
    // 获取用户信息
    edit (id, type) {
      this.dsId = id
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
            setFieldsValue(pick(record, ['dsId', 'name', 'config']))
          })
        })
      } else {
        resetFields()
      }
    },
    // 结束保存用户信息
    handleOk () {
      this.form.validateFields(async (err, values) => {
        if (!err) {
          values.type = 'custom'
          values.ds_id = this.dsId
          this.confirmLoading = true
          if (this.type === 'add') {
            addObj(values).then(res => {
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
            await putObj(values).then(res => {
              if (res.status === '0') {
                this.$emit('ok')
                this.confirmLoading = false
                // 清楚表单数据
                this.handleCancel()
                this.$message.success('修改成功')
              } else {
                this.confirmLoading = false
                this.$message.error(res.errstr)
              }
            }).catch(err => {
              this.confirmLoading = false
              this.$message.error(err.errstr)
            })
          }
        }
      })
    },
    handleCancel () {
      this.visible = false
      setTimeout(() => {
        this.addable = false
        this.showable = false
        this.editable = false
      }, 200)
    }
  }
}
</script>

<style scoped>

</style>
