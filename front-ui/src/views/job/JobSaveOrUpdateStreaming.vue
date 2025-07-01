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
                <div>
                  <span class="ds-icon">
                    <img :src="dsImgObj[table.type]" alt="">
                  </span>
                  <span>{{ table.name }}</span>
                </div>
              </a-select-option>
            </a-select>
          </a-col>
          <a-col :span="12">
            <label>目标数据源</label>
            <a-select v-model="selectedTargetSource" @change="handleToDsChange" placeholder="请选择目标数据源">
              <a-select-option v-for="table in toDsList" :value="table.dsId" :key="table.name">
                <div>
                  <span class="ds-icon">
                    <img :src="dsImgObj[table.type]" alt="">
                  </span>
                  <span>{{ table.name }}</span>
                </div>
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
            <a-select
              mode="combobox"
              :show-search="true"
              style="width: 100%"
              placeholder="目标topic"
              @change="handleToTbChange"
              v-model="selectedTargetTable">
              <a-select-option v-for="table in targetTables" :value="table" :key="table">
                {{ table }}
              </a-select-option>
            </a-select>
            <!--            <a-input v-model="selectedTargetTable" placeholder="目标topic"/>-->
          </a-col>
        </a-row>
      </a-form-item>
      <a-form-item
        label=""
      >
        <a-row :gutter="16">
          <a-col :span="6">
            开启断点续传: <a-switch @change="changeCheckpointConfig" />
          </a-col>
          <a-col :span="8"><p v-show="checkpoint">请选择断点续传字段下标: </p></a-col>
          <a-col :span="8" v-show="checkpoint">
            <!--            <a-input  />-->
            <a-select v-model="restore_column_index" placeholder="索引下标从0开始">
              <a-select-option v-for="(mapping, index) in mappings" :value="index" :key="index">
                {{ index }}
              </a-select-option>
            </a-select>
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
      <a-button key="forward" v-show="onlyRead" :loading="confirmLoading" type="primary" @click="handleSubmit">保存</a-button>
    </template>
  </a-modal>
</template>

<script>
  import { fetchTables, listQuery } from '@/api/datasource/datasource'
  import { streamAddObj, getObj, streamModifyObj } from '@/api/job/job'
  import LoadingDx from './../../components/common/loading-dx.vue'
  import { dsImgObj } from './../datasource/const'
  export default {
    components: {
      LoadingDx
    },
    data () {
      return {
        dsImgObj,
        jobType: 'default',
        form: this.$form.createForm(this),
        selectedDataSource: null,
        selectedTargetSource: null,
        selectedSourceTable: null,
        selectedTargetTable: '',
        confirmLoading: false,
        jobName: '',
        restore_column_index: '',
        checkpoint: false,
        onlyRead: true,
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
              'type': 1,
              'job_name': this.jobName,
              'sync_mode': {
                'checkpoint': this.checkpoint,
                'restore_column_index': this.restore_column_index
              }
            }
            console.log(this.jobId)
            console.log(this.type)
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
          }
        })
      },
      edit (type, id, jobType = 'default') {
        this.visible = true
        this.jobType = jobType
        this.type = type
        this.selectloading = true
        const excludeToDs = ['http', 'mysqlcdc']
        const streamDs = ['kafka', 'mysqlcdc']
        listQuery().then(res => {
          this.selectloading = false
          const record = res.result
          for (var a of record) {
            if (streamDs.includes(a.type)) {
              this.fromDsList.push({
                dsId: a.dsId,
                name: a.name,
                type: a.type
              })
            }
            if (!excludeToDs.includes(a.type)) {
              this.toDsList.push({
                dsId: a.dsId,
                name: a.name,
                type: a.type
              })
            }
          }
        })
        console.log(this.fromDsList)
        if (type === 'show') {
          this.onlyRead = false
        }
        if (type === 'edit' || type === 'show') {
          getObj(id).then(res => {
            const record = res.result
            this.selectedTargetSource = record.to_ds_id
            this.selectedDataSource = record.from_ds_id
            this.selectedSourceTable = record.from_tb_name
            this.selectedTargetTable = record.to_tb_name
            this.mappings = record.field_mappings
            this.jobId = record.job_id
            this.jobName = record.job_name
            this.checkpoint = record.sync_mode.checkpoint
            this.restore_column_index = record.sync_mode.restore_column_index
            fetchTables(this.selectedTargetSource).then(res => {
              this.targetTables = res.result
            })
            fetchTables(this.selectedDataSource).then(res => {
              this.sourceTables = res.result
            })
          })
        }
      },
      handleFromChange (value) {
        this.selectedDataSource = value
        // this.selectloading = true
        // console.log('当前选中数据源类型', this.selectedDataSource)
        // fetchTables(value).then(res => {
        //   this.selectloading = false
        //   this.sourceTables = res.result
        // })
      },
      changeCheckpointConfig (value) {
        this.checkpoint = value
      },
      handleToTbChange (value) {
        this.selectedTargetTable = value
      },
      handleToDsChange (value) {
        this.seyarnlectloading = true
        this.selectedTargetSource = value
        console.log('当前选中数据源类型', this.selectedTargetSource)
        fetchTables(value).then(res => {
          this.selectloading = false
          this.targetTables = res.result
        }).then(res => {
          this.selectloading = false
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
