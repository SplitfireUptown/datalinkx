<template>
  <a-card>
    <a-table :columns="columns" :dataSource="roleList" rowKey="roleId" :row-selection="roleSelection">
      <template v-slot:status="status">
        <a-badge :status="status === '0' ? 'success' : 'error'" :text="status === '0' ? '正常' : '停用'"/>
      </template>
      <template v-slot:action>
        <a-button type="primary" size="small" @click="editRole(text)">编辑</a-button>
        <a-button type="danger" size="small" @click="deleteRole(text)">删除</a-button>
      </template>
    </a-table>
  </a-card>
</template>
<script>
import { getRoleList } from '@/api/system/role'

export default {
  name: 'SystemRole',
  data () {
    return {
      roleList: [],
      selectedRowKeys: [],
      roleSelection: {
        onChange: (selectedRowKeys, selectedRows) => {
          console.log(`selectedRowKeys: ${selectedRowKeys}`, 'selectedRows: ', selectedRows)
        }
      },
      columns: [
        {
          title: '角色ID',
          dataIndex: 'roleId',
          key: 'roleId'
        },
        {
          title: '角色名称',
          dataIndex: 'roleName',
          key: 'roleName'
        },
        {
          title: '角色标识',
          dataIndex: 'roleKey',
          key: 'roleKey'
        },
        {
          title: '显示顺序',
          dataIndex: 'roleSort',
          key: 'roleSort'
        },
        {
          title: '数据范围',
          dataIndex: 'dataScope',
          key: 'dataScope'
        },
        {
          title: '角色状态',
          dataIndex: 'status',
          key: 'status',
          scopedSlots: { customRender: 'status' }
        },
        {
          title: '创建时间',
          dataIndex: 'createTime',
          key: 'createTime'
        },
        {
          title: '操作',
          key: 'action',
          scopedSlots: { customRender: 'action' }
        }
      ]
    }
  },
  methods: {
    getRoleList () {
      getRoleList().then(res => {
        this.roleList = res.result
      })
    }
  },
  mounted () {
    this.getRoleList()
  }
}
</script>
<style scoped lang="less">
.ant-btn{
  margin-right: 10px;
}
</style>
