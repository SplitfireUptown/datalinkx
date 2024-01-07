<template>
  <a-modal
    title="添加数据源"
    :width="640"
    :visible="visible"
    :maskClosable="false"
    @cancel="handleCancel"
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
          <a-col :span="12" >
            <label>来源数据源表</label>
            <a-select v-model="selectedSourceTable" @change="handleFromTbChange" placeholder="请选择来源数据源表">
              <a-select-option v-for="table in sourceTables" :value="table" :key="table" >
                {{ table }}
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :span="12">
            <label>目标数据源表</label>
            <a-select v-model="selectedTargetTable" @change="handleToTbChange" placeholder="请选择目标数据源表">
              <a-select-option v-for="table in targetTables" :value="table" :key="table">
                {{ table }}
              </a-select-option>
            </a-select>
          </a-col>
        </a-row>
      </a-form-item>

      <a-form-item
        label="同步配置"
      >
        <a-row :gutter="16">
          <a-col :span="6">
            <a-radio-group v-model="syncMode" button-style="solid" @change="changeSyncConfig">
              <a-radio-button value="overwrite">
                全量
              </a-radio-button>
              <a-radio-button value="increment">
                增量
              </a-radio-button>
            </a-radio-group>
          </a-col>
          <a-col :span="6"><p v-show="isIncrement">请选择增量字段: </p></a-col>
          <a-col :span="12">
            <a-select v-show="isIncrement" v-model="incrementField" placeholder="请选择增量字段">
              <a-select-option v-for="field in sourceFields" :value="field.name" :key="field.name">
                {{ field.name }}
              </a-select-option>
            </a-select>
          </a-col>
        </a-row>
      </a-form-item>
      <a-form-item
        label="定时配置（Spring crontab表达式）"
      >
        <a-input
          type="text"
          v-model="schedulerConf"/>
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
            <a-select v-model="mapping.sourceField" placeholder="请选择来源字段">
              <a-select-option v-for="field in sourceFields" :value="field.name" :key="field.name">
                {{ field.name }}
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :span="8">
            <a-select v-model="mapping.targetField" placeholder="请选择目标字段">
              <a-select-option v-for="field in targetFields" :value="field.name" :key="field.name">
                {{ field.name }}
              </a-select-option>
            </a-select>
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
    </a-form>

    <template slot="footer" >
        <a-button key="cancel" @click="handleCancel">取消</a-button>
        <a-button key="forward" :loading="confirmLoading" type="primary" @click="handleSubmit">保存</a-button>
    </template>
  </a-modal>
</template>

<script>
import { fetchTables, getDsTbFieldsInfo, listQuery } from '@/api/datasource/datasource'
import { addObj, getObj, modifyObj } from '@/api/job/job'
export default {
  data () {
    return {
      form: this.$form.createForm(this),
      selectedDataSource: null,
      selectedTargetSource: null,
      selectedSourceTable: null,
      selectedTargetTable: null,
      schedulerConf: '',
      confirmLoading: false,
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
      sourceFields: [],
      jobId: '',
      queryParam: {},
      targetFields: [],
      isIncrement: false,
      syncMode: 'overwrite',
      incrementField: ''
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
            'scheduler_conf': this.schedulerConf,
            'field_mappings': this.mappings,
            'sync_mode': {
              'mode': this.syncMode,
              'increate_field': this.incrementField
            }
          }
          console.log(this.jobId)
          if (this.type === 'edit') {
            await modifyObj(formData).catch(err => {
              this.$message.error(err.errstr)
            })
          } else {
            await addObj(formData)
          }
          setTimeout(() => {
            this.confirmLoading = false
            this.$emit('ok')
            this.visible = false
          }, 1500)
        }
      })
    },

    edit (type, id) {
      this.visible = true
      this.type = type
      listQuery().then(res => {
        const record = res.result
        for (var a of record) {
          this.fromDsList.push({
            'dsId': a.dsId,
            'name': a.name
          })
          this.toDsList.push({
            'dsId': a.dsId,
            'name': a.name
          })
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
          this.schedulerConf = record.scheduler_conf
          this.mappings = record.field_mappings
          this.jobId = record.job_id
          this.syncMode = record.sync_mode.mode
          console.log(this.syncMode)
          this.incrementField = record.sync_mode.increate_field
          if (this.syncMode === 'increment') {
            this.isIncrement = true
          }
          fetchTables(this.selectedTargetSource).then(res => {
            this.sourceTables = res.result
          })
          this.handleFromTbChange(this.selectedSourceTable)
          fetchTables(this.selectedDataSource).then(res => {
            this.targetTables = res.result
          })
          this.handleToTbChange(this.selectedTargetTable)
        })
      }
    },
    handleFromChange (value) {
      this.selectedDataSource = value
      fetchTables(value).then(res => {
        this.sourceTables = res.result
      })
    },
    handleToDsChange (value) {
      this.selectedTargetSource = value
      fetchTables(value).then(res => {
        this.targetTables = res.result
      })
    },
    handleFromTbChange (value) {
      this.selectedSourceTable = value
      this.queryParam = {
        'ds_id': this.selectedDataSource,
        'name': value
      }
      getDsTbFieldsInfo({
        ...this.queryParam
      }).then(res => {
        this.sourceFields = res.result
      })
    },
    changeSyncConfig (value) {
      if (value.target.value === 'increment') {
        this.isIncrement = true
        this.syncMode = 'increment'
      } else {
        this.isIncrement = false
        this.incrementField = ''
        this.syncMode = 'overwrite'
      }
    },
    handleToTbChange (value) {
      this.selectedTargetTable = value
      this.queryParam = {
        'ds_id': this.selectedTargetSource,
        'name': value
      }
      getDsTbFieldsInfo({
        ...this.queryParam
      }).then(res => {
        console.log(res)
        this.targetFields = res.result
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
      this.isIncrement = false
      this.syncMode = ''
      this.incrementField = ''
    },
    removeMapping (index) {
      this.mappings.splice(index, 1)
    }
  }
}
</script>
