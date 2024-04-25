<template>
<div class="ds-list-root">
  <div class="list-left">
    <ul>
      <li
      v-for="ds in dsTypeList"
      :key="ds.value"
      class="ds-list"
      :class="{active: ds.dsTypeKey === currentDs.dsTypeKey}"
      @click="selectDs(ds)"
      >
      <span class="ds-icon">
        <img :src="ds.img" alt="">
      </span>
      <div class="ds-name">
        <div class="nowrap">
          <span class="name">{{ds.label}}</span>
          <span class="num-in ml4">{{dsGroupNumber[ds.dsTypeKey]}}</span>
        </div>
      </div>
      </li>
    </ul>
  </div>
  <a-card :bordered="false" class="list-acard">
    <div class="table-page-search-wrapper">
      <a-form layout="inline">
        <a-row :gutter="48">
          <a-col :md="8" :sm="24">
            <a-form-item label="数据源名称">
              <a-input v-model="queryParam.name" placeholder="数据源名称"/>
            </a-form-item>
          </a-col>
          <a-col :md="8" :sm="24">
            <a-button @click="() => {this.queryData()}" type="primary">查询</a-button>
            <a-button @click="() => queryParam = {}" style="margin-left: 8px">重置</a-button>
          </a-col>
        </a-row>
      </a-form>
    </div>
    <div class="table-operator">
      <!--      v-action="'upms:user:add'"-->
      <a-button @click="$refs.refDsConfig.show('','add')" icon="plus" type="primary">新建</a-button>
    </div>
    <a-table
      :columns="columns"
      :dataSource="tableData"
      :loading="loading"
      :pagination="pagination"
      :rowKey="record => record.id"
      @change="handleTableChange"
    >
    </a-table>
    <ds-config
      @ok="handleOk"
      :currentDs="currentDs"
      ref="refDsConfig"
    />
  </a-card>
</div>
</template>

<script>
import { pageQuery, delObj, getDsGroup } from '@/api/datasource/datasource'
// import DsSaveOrUpdate from './DsSaveOrUpdate.vue'
import DsConfig from './DsConfig.vue'
import { dsTypeList } from './const'
import { DATA_SOURCE_TYPE } from '@/api/globalConstant'

export default {
  name: 'ContainerBottom',
  components: {
    // DsSaveOrUpdate,
    DsConfig
  },
  data () {
    return {
      loading: false,
      columns: [
        {
          title: 'ds_id',
          width: '10%',
          dataIndex: 'dsId'
        }, {
          title: '数据源名称',
          width: '10%',
          dataIndex: 'name'
        },
        {
          title: '数据源类型',
          width: '10%',
          dataIndex: 'type',
          customRender: (text) => {
            return (
              <div>
                {DATA_SOURCE_TYPE.map(item => {
                  if (item.value === text) {
                    return <span>{item.label}</span>
                  }
                })}
              </div>
            )
          }
        },
        {
          title: '创建时间',
          width: '10%',
          dataIndex: 'ctime',
          sorter: true
        }, {
          title: '操作',
          width: '15%',
          customRender: (record) => {
            return (
              <div>
                <a onClick={(e) => this.edit(record)}>修改</a>
                <a-divider type="vertical" />
                <a-popconfirm title="是否删除" onConfirm={() => this.delete(record)} okText="是" cancelText="否">
                  <a-icon slot="icon" type="question-circle-o" style="color: red" />
                  <a href="javascript:;" style="color: red">删除</a>
                </a-popconfirm>
                <a-divider type="vertical" />
                <a href="javascript:;"onClick={(e) => this.show(record)}>查看</a>
              </div>
            )
          }
        }
      ],
      tableData: [],
      pagination: {
        pageSize: 10,
        current: 1,
        total: 0,
        showSizeChanger: true
      },
      pages: {
        size: 10,
        current: 1
      },
      queryParam: {
      },
      dsTypeList,
      // 各数据源的数量
      dsGroupNumber: {
        1: 0,
        2: 0,
        3: 0,
        4: 0,
        100: 0
      },
      currentDs: {
        value: 'MySQL',
        label: 'MySQL',
        dsTypeKey: 1
      }
    }
  },
  methods: {
    init () {
      this.loading = true
      pageQuery({
        type: this.currentDs.dsTypeKey,
        ...this.queryParam,
        ...this.pages
      }).then(res => {
        this.tableData = res.result.data
        console.log(res)
        this.pagination.total = +res.result.total
        this.loading = false
      }).catch(reason => {
        this.loading = false
      })
    },
    getAllDsNumber () {
      getDsGroup().then(res => {
        this.loading = false
        if (res.status === '0') {
          this.dsGroupNumber = Object.assign({}, this.dsGroupNumber, res.result)
        } else {
          this.$message.error(res.error)
        }
      }).catch(reason => {
        this.loading = false
      })
    },
    selectDs (item) {
      this.currentDs = item
      this.pages.current = 1
      this.init()
    },
    handleTableChange (pagination, filters, sorter) {
      console.log(sorter.field)
      console.log(sorter.order)
      this.pagination = pagination
      this.pages.size = pagination.pageSize
      this.pages.current = pagination.current
      this.init()
    },

    edit (record) {
      this.$refs.refDsConfig.show(record.dsId, 'edit', record)
      // this.init()
    },
    delete (record) {
      console.log(record)
      // delObj(record.dsId).then(res => {
      //   this.$message.info('删除成功')
      //   this.init()
      //   this.getAllDsNumber()
      // })
      delObj(record.dsId).then(res => {
        if (res.status === '0') {
          this.$message.info('删除成功')
          this.init()
          this.getAllDsNumber()
        } else {
          this.$message.error(res.errstr)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    show (record) {
      this.$refs.refDsConfig.show(record.dsId, 'show', record)
    },
    handleOk (data) {
      this.init()
      if (data.type === 'add') {
        this.getAllDsNumber()
      }
    },
    queryData () {
      this.pages.current = 1
      this.init()
    }
  },
  created () {
    this.init()
    this.getAllDsNumber()
  }
}
</script>

<style scoped lang="less">
.ds-list-root {
  position: relative;
  display: flex;
  height: 100%;
  width: 100%;
  .list-left {
    width: 180px;
    height: 100%;
  }
  .list-acard {
    flex: 1;
    margin: 24px 0 16px 24px;
  }
  .list-left {
    border-radius: 0px;
    padding: 0 8px;
    overflow: auto;
    cursor: pointer;
    background-color: #fff;
    box-shadow: 0px 0px 1px rgba(15, 34, 67, 0.3), 0px 1px 3px rgba(15, 34, 67, 0.08), 0px 4px 8px rgba(15, 34, 67, 0.03);
    ul, li {
      list-style: none;
      padding: 0;
      margin: 0;
    }
    ul {
      margin-top: 4px;
    }
    .ds-list {
      line-height: 40px;
      padding-left: 16px;
      height: 40px;
      position: relative;
      border-radius: 6px;
      cursor: pointer;
      &:hover {
        background-color: rgba(15, 34, 67, 0.05);
      }
      .ds-icon {
        float: left;
        width: 24px;
        height: 24px;
        img {
          width: 24px;
          height: 24px;
          margin: 0;
          padding: 0;
          border: 0;
        }
      }
      .ds-name {
        margin-left: 8px;
        line-height: 20px;
        padding-top: 10px;
        padding-bottom: 10px;
        display: inline-block;
        max-width: 130px;
        .nowrap {
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
        .name {
          line-height: 20px;
          height: 20px;
          font-size: 14px;
          display: inline-block;
          color: rgba(21, 22, 24, 0.72);
          max-width: 90px;
          text-overflow: ellipsis;
          overflow: hidden;
          white-space: nowrap;
        }
        .num-in {
          top: 0;
          line-height: 14px;
          padding: 3px 4px;
          background-color: rgba(15, 34, 67, 0.07);
          color: rgba(15, 34, 67, 0.48);
          border-radius: 4px;
          font-size: 10px;
          display: inline-block;
          vertical-align: super;
          transform: scale(0.86);
        }
      }
    }
    .active {
      background-color: rgba(43, 121, 255, 0.1);
    }
  }

}
</style>
