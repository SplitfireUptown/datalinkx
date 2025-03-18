<template>
  <a-modal
    title="添加任务级联配置"
    :width="1000"
    :visible="visible"
    :confirmLoading="confirmLoading"
    :maskClosable="false"
    @cancel="handleCancel"
  >
    <a-spin :spinning="confirmLoading">
      <a-form :form="form">
        <a-form-item
          label="流转任务"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-select
            v-decorator="['job_id', {rules: [{required: true, message: '选择流转任务'}]}]"
            :options="selectJobs"
            v-model="job_id"
            style="display: inline-block; width: 70%"
          >
          </a-select>
          <a-button type="link" icon="arrows-alt" @click="edit(job_id)">详情</a-button>
        </a-form-item>

        <a-form-item
          label="流转子任务"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-select
            v-decorator="['sub_job_id', {rules: [{required: true, message: '选择级联触发流转子任务'}]}]"
            :options="selectJobs"
            v-model="sub_job_id"
            style="display: inline-block; width: 70%"
          >
          </a-select>
          <a-button type="link" icon="arrows-alt" @click="edit(sub_job_id)">详情</a-button>
        </a-form-item>
        <a-form-item
          label="级联权重"
          :labelCol="labelCol"
          :wrapperCol="wrapperCol"
        >
          <a-input
            type="number"
            style="display: inline-block; width: 70%"
            v-decorator="['priority', {rules: [{required: true, message: '请输入级联权重'}]}]" />
        </a-form-item>
      </a-form>
    </a-spin>
    <template slot="footer" >
      <div
        v-show="addable"
      >
        <a-button key="cancel" @click="handleCancel">取消</a-button>
        <a-button key="forward" :loading="confirmLoading" type="primary" @click="handleOk">保存</a-button>
      </div>
    </template>
    <job-save-or-update
      @ok="handleOk"
      ref="JobSaveOrUpdate"
    />
  </a-modal>

</template>

<script>
  import { addObj } from '@/api/job/jobrelation.js'
  import { listQuery } from '@/api/job/job'
  import JobSaveOrUpdate from '../job/JobSaveOrUpdate.vue'
  const selectTables = []

  export default {
    name: 'JobRelationSave',
    components: {
      JobSaveOrUpdate
    },
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
        selectJobs: [],
        job_id: '',
        sub_job_id: '',
        visible: false,
        confirmLoading: false,
        form: this.$form.createForm(this),
        addable: false,
        type: 'add'
      }
    },
    created () {
      this.init()
    },
    methods: {
      // 获取用户信息
      add () {
        this.visible = true
        this.addable = true
      },
      edit (jobId) {
        if (jobId === '') {
          return
        }
        this.$refs.JobSaveOrUpdate.readOnly(jobId)
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
            }).finally(res => {
              this.job_id = ''
              this.sub_job_id = ''
            })
          }
        })
      },
      handleCancel () {
        this.visible = false
        this.job_id = ''
        this.sub_job_id = ''
        setTimeout(() => {
          this.addable = false
        }, 200)
      },
      init () {
        listQuery().then(res => {
          for (const i of res.result) {
            if (i.type !== 1) {
              this.selectJobs.push({
                'label': i.job_name,
                'value': i.job_id
              })
            }
          }
          console.log(this.selectJobs)
        }).finally(() => {
          this.loading = false
        })
      },
      handleSearch (value) {

      }
    }
  }
</script>

<style scoped>

</style>