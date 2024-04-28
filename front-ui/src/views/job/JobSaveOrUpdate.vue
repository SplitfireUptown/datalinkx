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
            <a-select v-model="selectedSourceTable" @change="handleFromTbChange" placeholder="请选择来源数据源表">
              <a-select-option v-for="table in sourceTables" :value="table" :key="table" >
                {{ table }}
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :span="12" v-show="!isRedisTo">
            <div class="redis-type-lable">
              <label>目标数据源表</label>
            </div>
            <a-select v-model="selectedTargetTable" @change="handleToTbChange" placeholder="请选择目标数据源表">
              <a-select-option v-for="table in targetTables" :value="table" :key="table">
                {{ table }}
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :span="12" v-show="isRedisTo" class="job-save-col">
            <div class="redis-type-lable">
              <label class="redis-lable">type:</label>
              <label class="redis-lable">key:</label>
            </div>
            <div class="redis-type-val">
              <a-select v-model="redisToType" @change="changeToRedisType" placeholder="请选择Type">
                <a-select-option v-for="item in RedisTypes" :value="item.value" :key="item.value" >
                  {{ item.label }}
                </a-select-option>
              </a-select>
              <a-input
                type="text"
                placeholder="请输入Value"
                v-model="redisToValue"/>
            </div>
          </a-col>
        </a-row>
      </a-form-item>

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
          <a-col :span="6" v-show="!isIncrement">开启数据覆盖: <a-switch v-model="cover" @change="changeCover" /></a-col>
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
        v-show="!isStreaming"
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
      <LoadingDx size="'size-1x'" v-if="selectloading"></LoadingDx>
    </a-form>

    <template slot="footer">
      <a-button key="cancel" @click="handleCancel">取消</a-button>
      <a-button key="forward" v-show='onlyRead' :loading="confirmLoading" type="primary" @click="handleSubmit">保存</a-button>
    </template>
  </a-modal>
</template>

<script>
import { fetchTables, getDsTbFieldsInfo, listQuery } from '@/api/datasource/datasource'
import { addObj, getObj, modifyObj } from '@/api/job/job'
import LoadingDx from './../../components/common/loading-dx.vue'
import { RedisTypes } from './../datasource/const'
export default {
  components: {
    LoadingDx
  },
  data () {
    return {
      RedisTypes,
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
      redisToValue: ''
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
    }
  },
  methods: {
    handleSubmit () {
      this.form.validateFields(async (err, values) => {
        this.confirmLoading = true
        if (!err) {
          this.confirmLoading = true
          if (this.redisToValue !== '') {
            this.selectedTargetTable = (this.redisToType + this.redisSpitKey + this.redisToValue)
          }

          const formData = {
            'job_id': this.jobId,
            'from_ds_id': this.selectedDataSource,
            'to_ds_id': this.selectedTargetSource,
            'from_tb_name': this.selectedSourceTable,
            'to_tb_name': this.selectedTargetTable,
            'scheduler_conf': this.schedulerConf,
            'field_mappings': this.mappings,
            'job_name': this.jobName,
            'cover': this.cover,
            'sync_mode': {
              'mode': this.syncMode,
              'increate_field': this.incrementField
            }
          }
          console.log(this.jobId)
          if (this.type === 'edit') {
            modifyObj(formData).catch(err => {
              this.$message.error(err.errstr)
            })
          } else {
            addObj(formData).then(res => {
              if (res.status !== '0') {
                this.$message.error(res.errstr)
              }
            })
          }
          setTimeout(() => {
            this.confirmLoading = false
            this.$emit('ok')
            this.visible = false
          }, 1500)
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
      this.selectloading = true
      fetchTables(value).then(res => {
        this.selectloading = false
        this.sourceTables = res.result
      })
    },
    handleToDsChange (value) {
      this.selectedTargetSource = value
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
      if (checked) {
        this.cover = 1
      } else {
        this.cover = 0
      }
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
    .redis-type-val {
      display: flex;
      width: 100%;
      margin-top: 4px;
    }
  }
}
</style>
