<template>
  <a-card title="角色管理">
    <div style="display: flex">
      <a-input-search
        style="margin-right: 200px;margin-bottom: 8px"
        placeholder="搜索"
        @change="onChange"
        :allowClear="true" />
      <a-button type="primary" style="margin-bottom: 8px" @click="editRole({})">新增</a-button>
      <a-button type="primary" style="margin-bottom: 8px" @click="deleteRole(checkedKeys)">删除</a-button>
    </div>
    <div>
      <a-table :columns="columns" :dataSource="showRoleList" rowKey="roleId" :row-selection="rowSelection">
        <template v-slot:status="status">
          <a-badge :status="status === '0' ? 'success' : 'error'" :text="status === '0' ? '正常' : '停用'" />
        </template>
        <template v-slot:action="record">
          <a-button type="primary" size="small" @click="editRole(record)">编辑</a-button>
          <a-button type="danger" size="small" @click="deleteRole(record)">删除</a-button>
          <a @click="assignUsers(record)">分配用户</a>
          <a @click="assignMenus(record)">分配菜单</a>
        </template>
      </a-table>
    </div>
    <EditRole @editRoleSuccess="getRoleList()" :role="role" v-if="visible" :visible.sync="visible" />
    <a-modal
      title="分配用户"
      :visible="assignUsersVisible"
      @cancel="assignUsersVisible = false"
      width="65vw"
      @ok="createAuthRoleUserList">
      <UserTable
        :userSelection.sync="selectedUserRowKeys"
      />
    </a-modal>
    <a-modal
      title="分配菜单"
      :visible="assignMenusVisible"
      @cancel="assignMenusVisible = false"
      width="65vw"
      @ok="createAuthRoleMenuList">
      <menuTable :menuSelection.sync="selectedMenuRowKeys" />
    </a-modal>
  </a-card>
</template>
<script>
import {
  createAuthRoleMenuList,
  createAuthRoleUserList,
  deleteRole, getAuthMenuList,
  getAuthUserList,
  getRoleList
} from '@/api/system/role'
import EditRole from '@/views/system/role/editRole.vue'
import UserTable from '@/views/user/UserTable.vue'
import MenuTable from '@/views/system/menu/menuTable.vue'

export default {
  name: 'SystemRole',
  components: { MenuTable, UserTable, EditRole },
  data () {
    return {
      roleList: [],
      showRoleList: [],
      userList: [],
      selectedRowKeys: [],
      checkedKeys: [],
      selectedMenuRowKeys: [],
      selectedUserRowKeys: [],
      role: {},
      visible: false,
      assignUsersVisible: false,
      assignMenusVisible: false,
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
          title: '备注',
          dataIndex: 'remark',
          key: 'remark'
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
        if (res.status === '0') {
          this.roleList = res.result
          this.showRoleList = res.result
        } else {
          this.$message.error('获取角色列表失败')
        }
      }).catch(() => {
        this.$message.error('获取角色列表失败')
      })
    },
    getAuthUserList (record) {
      getAuthUserList(record.roleId).then(res => {
        if (res.status === '0') {
          this.selectedUserRowKeys = res.result.userList.map(item => item.userId)
          this.assignUsersVisible = true
        } else {
          this.$message.error('获取用户列表失败')
        }
      }).catch(() => {
        this.$message.error('获取用户列表失败')
      })
    },
    getAuthMenuList (record) {
      getAuthMenuList(record.roleId).then(res => {
        if (res.status === '0') {
          this.selectedMenuRowKeys = res.result.menuList.map(item => item.menuId)
          this.assignMenusVisible = true
        } else {
          this.$message.error('获取菜单列表失败')
        }
      }).catch(() => {
        this.$message.error('获取菜单列表失败')
      })
    },
    editRole (record) {
      this.role = record
      this.visible = true
    },
    deleteRole (record) {
      record = record instanceof Array ? record : [record.roleId]
      this.$confirm({
        title: '提示',
        content: record.length > 1 ? '确定删除选中的角色吗？' : '确定删除该角色吗？',
        onOk: () => {
          deleteRole(record).then(res => {
            this.getRoleList()
            if (res.status === '0') {
              this.$message.success('删除成功')
            } else {
              this.$message.error('删除失败')
            }
          }).catch(() => {
            this.$message.error('删除失败')
          })
        }
      })
    },
    assignUsers (record) {
      this.role = record
      this.getAuthUserList(record)
    },
    assignMenus (record) {
      this.role = record
      this.getAuthMenuList(record)
    },
    createAuthRoleUserList () {
      createAuthRoleUserList({ roleId: this.role.roleId, userIds: this.selectedUserRowKeys }).then(res => {
        if (res.status === '0') {
          this.$message.success('分配成功')
          this.assignUsersVisible = false
        } else {
          this.$message.error('分配失败')
        }
      }).catch(() => {
        this.$message.error('分配失败')
      })
    },
    createAuthRoleMenuList () {
      createAuthRoleMenuList({ roleId: this.role.roleId, menuIds: this.selectedMenuRowKeys }).then(res => {
        if (res.status === '0') {
          this.$message.success('分配成功')
          this.assignMenusVisible = false
        } else {
          this.$message.error('分配失败')
        }
      }).catch(() => {
        this.$message.error('分配失败')
      })
      console.log('selectedMenuRowKeys', this.selectedMenuRowKeys)
    },
    onChange (e) {
      if (!e.target.value) {
        this.showRoleList = this.roleList
      } else {
        this.showRoleList = this.roleList.filter(item => item.roleName.includes(e.target.value))
      }
    },
    changeSelectedRowKeys (checkedKeys) {
      this.checkedKeys = checkedKeys
    }
  },
  mounted () {
    this.getRoleList()
  },
  computed: {
    rowSelection () {
      return {
        selectedRowKeys: this.checkedKeys,
        onChange: (selectedRowKeys) => {
          this.changeSelectedRowKeys(selectedRowKeys)
        }
      }
    }
  }
}
</script>
<style scoped lang="less">
.ant-btn {
  margin-right: 10px;
}

a {
  margin-right: 10px;
}
</style>
