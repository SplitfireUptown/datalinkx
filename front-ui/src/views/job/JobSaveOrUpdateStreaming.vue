<template>
  <a-modal
    title="新增流式流转任务"
    :width="640"
    :visible="visible"
    :maskClosable="false"
    @cancel="handleCancel"
    class="job-save-root"
  >
    <a-form :form="form" @submit="handleSubmit" style="max-width: 600px">
      <a-form-item
        v-show="false"
        label="job_id"
      >
        <a-input
          v-decorator="['dsId']"
        />
      </a-form-item>
      <a-form-item
        label="任务名称"
      >
        <a-input
          type="text"
          v-model="jobName"/>
      </a-form-item>
      <a-form-item>
        <a-row :gutter="16">
          <a-col :span="12" >
            <label>来源数据源</label>
            <a-select v-model="selectedDataSource" @change="handleFromChange" placeholder="请选择来源数据源">
              <a-select-option v-for="table in fromDsList" :value="table.dsId" :key="table.name">
                {{ table.name }}
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :span="12">
            <label>目标数据源</label>
            <a-select v-model="selectedTargetSource" @change="handleToDsChange" placeholder="请选择目标数据源">
              <a-select-option v-for="table in toDsList" :value="table.dsId" :key="table.name">
                {{ table.name }}
              </a-select-option>
            </a-select>
          </a-col>
        </a-row>
      </a-form-item>

      <a-form-item>
        <a-row :gutter="16">
          <a-col :span="12" class="job-save-col">
            <div class="redis-type-lable">
              <label>来源数据源表</label>
            </div>
            <a-input v-model="selectedSourceTable" placeholder="来源topic"/>
          </a-col>
          <a-col :span="12">
            <div class="redis-type-lable">
              <label>目标数据源表</label>
            </div>
            <a-input v-model="selectedTargetTable" placeholder="目标topic"/>
          </a-col>
        </a-row>
      </a-form-item>

      <a-form-item label="字段映射关系">
        <a-row :gutter="16">
          <a-col :span="8">
            <span>来源字段</span>
          </a-col>
          <a-col :span="8">
            <span>目标字段</span>
          </a-col>
        </a-row>
        <a-row :gutter="16" v-for="(mapping, index) in mappings" :key="index">
          <a-col :span="8">
            <a-input v-model="mapping.sourceField" placeholder="来源topic字段"/>
          </a-col>
          <a-col :span="8">
            <a-input v-model="mapping.targetField" placeholder="目标topic字段"/>
          </a-col>
          <a-col :span="4">
            <a-icon type="minus-circle-o" @click="removeMapping(index)" v-show="mappings.length > 1" />
          </a-col>
        </a-row>
        <a-row :gutter="16">
          <a-col :span="24">
            <a-button type="dashed" @click="addMapping" style="width: 100%">
              <a-icon type="plus" /> 添加字段映射关系
            </a-button>
          </a-col>
        </a-row>
      </a-form-item>
      <LoadingDx size="'size-1x'" v-if="selectloading"></LoadingDx>
    </a-form>

    <template slot="footer">
      <a-button key="cancel" @click="handleCancel">取消</a-button>
      <a-button key="forward" v-show='onlyRead' :loading="confirmLoading" type="primary" @click="handleSubmit">保存</a-button>
    </template>
  </a-modal>
</template>

<script>
import { fetchTables, listQuery } from '@/api/datasource/datasource'
import { streamAddObj, getObj, streamModifyObj } from '@/api/job/job'
import LoadingDx from './../../components/common/loading-dx.vue'
export default {
  components: {
    LoadingDx
  },
  data () {
    return {
      jobType: 'default',
      form: this.$form.createForm(this),
      selectedDataSource: null,
      selectedTargetSource: null,
      selectedSourceTable: null,
      selectedTargetTable: null,
      confirmLoading: false,
      jobName: '',
      onlyRead: true,
      dataSources: [
        { id: 1, name: '数据源1' },
        { id: 2, name: '数据源2' },
        { id: 3, name: '数据源3' }
        // 其他数据源选项
      ],
      visible: false,
      type: '',
      fromDsList: [],
      toDsList: [],
      sourceTables: [],
      targetTables: [],
      mappings: [
        { sourceField: '', targetField: '' }
      ],
      jobId: '',
      queryParam: {},
      selectloading: false
    }
  },
  computed: {
    isStreaming () {
      return this.jobType === 'streaming'
    }
  },
  methods: {
    handleSubmit () {
      this.form.validateFields(async (err, values) => {
        this.confirmLoading = true
        if (!err) {
          this.confirmLoading = true

          const formData = {
            'job_id': this.jobId,
            'from_ds_id': this.selectedDataSource,
            'to_ds_id': this.selectedTargetSource,
            'from_tb_name': this.selectedSourceTable,
            'to_tb_name': this.selectedTargetTable,
            'field_mappings': this.mappings,
            'job_name': this.jobName
          }
          console.log(this.jobId)
          if (this.type === 'edit') {
            streamModifyObj(formData).then(res => {
              this.confirmLoading = false
              this.$emit('ok')
              this.visible = false
            }).catch(err => {
              this.$message.error(err.errstr)
            })
          } else {
            streamAddObj(formData).then(res => {
              if (res.status !== '0') {
                this.confirmLoading = false
                this.$emit('ok')
                this.visible = false
                this.$message.error(res.errstr)
              }
            })
          }
          // setTimeout(() => {
          //   this.confirmLoading = false
          //   this.$emit('ok')
          //   this.visible = false
          // }, 1500)
        }
      })
    },
    readOnly (id) {
      this.onlyRead = false
      console.log(this.onlyRead)
      this.edit('edit', id)
    },
    edit (type, id, jobType = 'default') {
      this.visible = true
      this.jobType = jobType
      this.type = type
      this.selectloading = true
      listQuery().then(res => {
        this.selectloading = false
        const record = res.result
        for (var a of record) {
          if (a.type >= 100) {
            this.fromDsList.push({
              dsId: a.dsId,
              name: a.name,
              type: a.type
            })
            this.toDsList.push({
              dsId: a.dsId,
              name: a.name,
              type: a.type
            })
          }
        }
      })
      console.log(this.fromDsList)
      if (type === 'edit') {
        getObj(id).then(res => {
          const record = res.result
          this.selectedTargetSource = record.to_ds_id
          this.selectedDataSource = record.from_ds_id
          this.selectedSourceTable = record.from_tb_name
          this.selectedTargetTable = record.to_tb_name
          this.mappings = record.field_mappings
          this.jobId = record.job_id
          this.jobName = record.job_name
          console.log(this.syncMode)

          this.incrementField = record.sync_mode.increate_field
          fetchTables(this.selectedTargetSource).then(res => {
            this.targetTables = res.result
          })
          this.handleFromTbChange(this.selectedSourceTable)
          fetchTables(this.selectedDataSource).then(res => {
            this.sourceTables = res.result
          })
        })
      }
    },
    handleFromChange (value) {
      this.selectedDataSource = value
      this.selectloading = true
      console.log('当前选中数据源类型', this.selectedDataSource)
      fetchTables(value).then(res => {
        this.selectloading = false
        this.sourceTables = res.result
      })
    },
    handleToDsChange (value) {
      this.selectedTargetSource = value
      console.log('当前选中数据源类型', this.selectedTargetSource)
      fetchTables(value).then(res => {
        this.selectloading = false
        this.targetTables = res.result
      })
    },
    addMapping () {
      this.mappings.push({ sourceField: '', targetField: '' })
    },
    handleCancel () {
      this.visible = false
      this.selectedDataSource = null
      this.selectedTargetSource = null
      this.selectedSourceTable = null
      this.selectedTargetTable = null
      this.fromDsList = []
      this.toDsList = []
      this.sourceTables = []
      this.targetTables = []
      this.mappings = []
      this.jobName = ''
    },
    removeMapping (index) {
      this.mappings.splice(index, 1)
    }
  }
}
</script>

<style scoped lang="less">
.job-save-root {
  .job-save-col {
    .redis-type-lable {
      display: flex;
      .redis-lable {
        display: block;
        width: 48%;
      }
    }
  }
}
</style>
