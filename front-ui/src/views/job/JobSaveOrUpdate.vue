<template>
  <a-modal
    title="新增数据流转任务"
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
          v-decorator="['jobName', {rules: [{required: true, message: '请输入任务名称'}],initialValue: jobName}]"
        />
      </a-form-item>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="来源数据源">
            <a-select
              @change="handleFromChange"
              v-decorator="['selectedDataSource', {rules: [{required: true, message: '请选择来源数据源'}],initialValue: selectedDataSource}]">
              <a-select-option v-for="table in fromDsList" :value="table.dsId" :key="table.name">
                <div>
                  <span class="ds-icon">
                    <img :src="dsImgObj[table.type]" alt="">
                  </span>
                  <span>{{ table.name }}</span>
                </div>
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12">
          <a-form-item label="目标数据源">
            <a-select
              @change="handleToDsChange"
              v-decorator="['selectedTargetSource', {rules: [{required: true, message: '请选择目标数据源'}],initialValue: selectedTargetSource}]">
              <a-select-option v-for="table in toDsList" :value="table.dsId" :key="table.name">
                <div>
                  <span class="ds-icon">
                    <img :src="dsImgObj[table.type]" alt="">
                  </span>
                  <span>{{ table.name }}</span>
                </div>
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
      </a-row>
      <a-row :gutter="16">
        <a-col :span="12">
          <a-form-item label="来源数据源表">
            <a-select @change="handleFromTbChange" v-decorator="['selectedSourceTable', {rules: [{required: true, message: '请选择来源数据源表'}],initialValue: selectedSourceTable}]">
              <a-select-option v-for="table in sourceTables" :value="table" :key="table">
                {{ table }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12" v-show="!isRedisTo">
          <a-form-item label="目标数据源表">
            <a-select @change="handleToTbChange" v-decorator="['selectedTargetTable', {rules: [{required: true, message: '请选择目标数据源表'}],initialValue: selectedTargetTable}]">
              <a-select-option v-for="table in targetTables" :value="table" :key="table">
                {{ table }}
              </a-select-option>
            </a-select>
          </a-form-item>
        </a-col>
        <a-col :span="12" v-show="isRedisTo" class="job-save-col">
          <div class="redis-type-val">
            <a-form-item ref="redis_type" label="type:" :required="true">
              <a-select v-model="redisToType" @change="changeToRedisType;validate_redis" :validateStatus="redis_type_validateStatus" :help="redis_type_help">
                <a-select-option v-for="item in RedisTypes" :value="item.value" :key="item.value">
                  {{ item.label }}
                </a-select-option>
              </a-select>
            </a-form-item>
            <a-form-item ref="redis_value" label="key:" :required="true" :validateStatus="redis_value_validateStatus" :help="redis_value_help">
              <a-input
                @blur="validate_redis"
                type="text"
                v-model="redisToValue"/>
            </a-form-item>
          </div>
        </a-col>
      </a-row>
      <a-form-item
        label="同步配置"
        v-show="!isRedisTo && !isStreaming"
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
          <a-col :span="6" v-show="!isIncrement">开启数据覆盖: <a-switch v-model="trans_cover" @change="changeCover" /></a-col>
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
        v-show="!isStreaming"
      >
        <a href="https://tool.lu/crontab" target="_blank">定时配置（Spring crontab表达式）</a>
        <a-input
          type="text"
          v-decorator="['schedulerConf', {rules: [{required: true, message: '请输入crontab表达式', trigger: 'blur'}],initialValue: schedulerConf}]"/>
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
      <LoadingDx size="'size-1x'" v-if="selectloading"></LoadingDx>
    </a-form>

    <template slot="footer">
      <a-button key="cancel" @click="handleCancel">取消</a-button>
      <a-button key="forward" v-show="onlyRead" :loading="confirmLoading" type="primary" @click="handleSubmit">保存</a-button>
    </template>
  </a-modal>
</template>

<script>
import { fetchTables, getDsTbFieldsInfo, listQuery } from '@/api/datasource/datasource'
import { addObj, getObj, modifyObj } from '@/api/job/job'
import LoadingDx from './../../components/common/loading-dx.vue'
import { RedisTypes, dsImgObj } from './../datasource/const'
export default {
  components: {
    LoadingDx
  },
  data () {
    return {
      RedisTypes,
      dsImgObj,
      jobType: 'default',
      form: this.$form.createForm(this),
      selectedDataSource: null,
      selectedTargetSource: null,
      selectedSourceTable: null,
      selectedTargetTable: null,
      schedulerConf: '',
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
      redisSpitKey: '!-!-!',
      mappings: [
        { sourceField: '', targetField: '' }
      ],
      sourceFields: [],
      jobId: '',
      queryParam: {},
      targetFields: [],
      isIncrement: false,
      syncMode: 'overwrite',
      cover: 0,
      incrementField: '',
      selectloading: false,
      redisToType: 'string',
      redisToValue: '',
      redis_type_validateStatus: '',
      redis_value_validateStatus: '',
      redis_type_help: '',
      redis_value_help: ''
    }
  },
  computed: {
    isRedisTo () {
      let temp = false
      if (this.selectedTargetSource) {
        temp = this.toDsList.find(item => { return item.dsId === this.selectedTargetSource })?.type === 4
      }
      return temp
    },
    isStreaming () {
      return this.jobType === 'streaming'
    },
    // 是否覆盖
    trans_cover () {
      return Boolean(this.cover)
    }
  },
  methods: {
    // 校验redis
    validate_redis () {
      if (!this.redisToType) {
        this.redis_type_validateStatus = 'error'
        this.redis_type_help = '请选择Type'
      } else {
        this.redis_type_validateStatus = ''
        this.redis_type_help = ''
      }
      if (!this.redisToValue) {
        this.redis_value_validateStatus = 'error'
        this.redis_value_help = '请输入Value'
      } else {
        this.redis_value_validateStatus = ''
        this.redis_value_help = ''
      }
    },
    handleSubmit () {
      // 如果目标数据源是redis 则设置为全量
      if (this.isRedisTo) {
        // 同步redis数据源表单数据
        this.form.setFieldsValue({ 'selectedTargetTable': this.redisToType + this.redisSpitKey + this.redisToValue })
        // 则校验redis数据
        this.validate_redis()
      }
      this.form.validateFields(async (err, values) => {
        if (!err) {
          this.confirmLoading = true
          const formData = {
            'job_id': this.jobId,
            // 提交表单数据
            'from_ds_id': this.form.getFieldValue('selectedDataSource'),
            'to_ds_id': this.form.getFieldValue('selectedTargetSource'),
            'from_tb_name': this.form.getFieldValue('selectedSourceTable'),
            'to_tb_name': this.form.getFieldValue('selectedTargetTable'),
            'job_name': this.form.getFieldValue('jobName'),
            'scheduler_conf': this.form.getFieldValue('schedulerConf'),
            'field_mappings': this.mappings,
            'cover': this.cover,
            'sync_mode': {
              'mode': this.syncMode,
              'increate_field': this.incrementField
            }
          }
          console.log(this.jobId)
          if (this.type === 'edit') {
            modifyObj(formData).then(res => {
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
          } else {
            addObj(formData).then(res => {
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
          // redis数据源暂不支持读
          if (a.type !== 4) {
            this.fromDsList.push({
              dsId: a.dsId,
              name: a.name,
              type: a.type
            })
          }
          this.toDsList.push({
            dsId: a.dsId,
            name: a.name,
            type: a.type
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
          this.jobName = record.job_name
          this.cover = record.cover
          console.log(this.syncMode)
          if (this.selectedTargetTable.includes(this.redisSpitKey)) {
            const arr = this.selectedTargetTable.split(this.redisSpitKey)
            this.redisToValue = arr[1]
            this.redisToType = arr[0]
          }

          this.incrementField = record.sync_mode.increate_field
          if (this.syncMode === 'increment') {
            this.isIncrement = true
          }
          fetchTables(this.selectedTargetSource).then(res => {
            this.targetTables = res.result
          })
          this.handleFromTbChange(this.selectedSourceTable)
          fetchTables(this.selectedDataSource).then(res => {
            this.sourceTables = res.result
          })
          this.handleToTbChange(this.selectedTargetTable)
        })
      }
    },
    handleFromChange (value) {
      this.selectedDataSource = value
      // 切换数据源同步表单数据
      this.form.setFieldsValue({ 'selectedDataSource': value })
      // 清空表单中来源表
      this.form.setFieldsValue({ 'selectedSourceTable': '' })
      this.selectloading = true
      fetchTables(value).then(res => {
        this.selectloading = false
        this.sourceTables = res.result
      })
    },
    handleToDsChange (value) {
      this.selectedTargetSource = value
      // 切换目标数据源同步表单数据
      this.form.setFieldsValue({ 'selectedTargetSource': value })
      // 清空表单中目标表
      this.form.setFieldsValue({ 'selectedTargetTable': '' })
      console.log('当前选中数据源类型', this.selectedTargetSource, this.isRedisTo)
      // 如果目标数据源是redis 则设置为全量
      if (this.toDsList.find(item => { return item.dsId === this.selectedTargetSource })?.type === 4) {
        this.syncMode = 'overwrite'
        this.isIncrement = false
      } else {
        this.selectloading = true
        fetchTables(value).then(res => {
          this.selectloading = false
          this.targetTables = res.result
        })
      }
    },
    handleFromTbChange (value) {
      this.selectloading = true
      // 切换来源表同步表单数据
      this.form.setFieldsValue({ 'selectedSourceTable': value })
      this.selectedSourceTable = value
      this.queryParam = {
        'ds_id': this.selectedDataSource,
        'name': value
      }
      getDsTbFieldsInfo({
        ...this.queryParam
      }).then(res => {
        this.selectloading = false
        this.sourceFields = res.result
      })
    },
    // 目标数据源为redis时切换类型
    changeToRedisType (val) {
      console.log(val)
    },
    changeCover (checked) {
      this.cover = checked ? 1 : 0
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
      this.selectloading = true
      this.selectedTargetTable = value
      // 切换目标表同步表单数据
      this.form.setFieldsValue({ 'selectedTargetTable': value })
      this.queryParam = {
        'ds_id': this.selectedTargetSource,
        'name': value
      }
      getDsTbFieldsInfo({
        ...this.queryParam
      }).then(res => {
        this.selectloading = false
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
      this.mappings = []
      this.syncMode = ''
      this.cover = 0
      this.incrementField = ''
      this.schedulerConf = ''
      this.jobName = ''
      this.syncMode = 'overwrite'
      this.form.resetFields()
      this.redis_value_help = ''
      this.redis_type_help = ''
      this.redis_value_validateStatus = ''
      this.redis_type_validateStatus = ''
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
    .redis-type-val {
      display: flex;
      :nth-child(2)
      {
        flex-grow: 1;
      }
    }
  }
}

::v-deep .ds-icon {
  float: left;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  overflow: hidden;
  margin-right: 4px;
  img {
    width: 24px;
    height: 24px;
    margin: 0;
    padding: 0;
    border: 0;
  }
}
</style>
