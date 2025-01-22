<template>
  <a-card title="用户列表">
    <div style="display: flex">
      <a-input-search
        style="margin-right: 200px;margin-bottom: 8px"
        placeholder="搜索"
        @change="onChange"
        :allowClear="true" />
    </div>
    <div>
      <a-table
        :columns="columns"
        :data-source="showUserList"
        row-key="userId"
        :row-selection="rowSelection"
        :tableLayout="'fixed'">
        <template v-slot:sex="sex">
          <span>{{ sexed[sex] }}</span>
        </template>
      </a-table>
    </div>
  </a-card>
</template>
<script>
import { getUserList } from '@/api/user'

export default {
  name: 'UserTable',
  props: {
    userSelection: {
      type: Array,
      default: () => []
    }
  },
  data () {
    return {
      userList: [],
      showUserList: [],
      columns: [
        {
          title: '用户ID',
          dataIndex: 'userId',
          key: 'userId'
        },
        {
          title: '用户名',
          dataIndex: 'userName',
          key: 'userName'
        },
        {
          title: '用户昵称',
          dataIndex: 'nickName',
          key: 'nickName'
        },
        {
          title: '用户邮箱',
          dataIndex: 'email',
          key: 'email'
        },
        {
          title: '手机号码',
          dataIndex: 'phonenumber',
          key: 'phonenumber'
        },
        {
          title: '性别',
          dataIndex: 'sex',
          key: 'sex',
          scopedSlots: { customRender: 'sex' }
        }
      ]
    }
  },
  methods: {
    getUserList () {
      getUserList().then(res => {
        if (res.status === '0') {
          this.userList = res.result
          this.showUserList = res.result
        } else {
          this.$message.error('获取用户列表失败')
        }
      }).catch(() => {
        this.$message.error('获取用户列表失败')
      })
    },
    onChange (e) {
      if (!e.target.value) {
        this.showUserList = this.userList
      } else {
        e = e.target.value
        this.showUserList = this.userList.filter(user => {
          return user.userName.includes(e) || user.nickName.includes(e) || user.email.includes(e) || user.phonenumber.includes(e)
        })
      }
    }
  },
  computed: {
    sexed () {
      return {
        0: '男',
        1: '女',
        2: '未知'
      }
    },
    rowSelection () {
      return {
        selectedRowKeys: this.userSelection,
        onChange: (selectedRowKeys) => {
          this.$emit('update:userSelection', selectedRowKeys)
        }
      }
    }
  },
  mounted () {
    this.getUserList()
  }
}
</script>
<style scoped lang="less">

</style>
