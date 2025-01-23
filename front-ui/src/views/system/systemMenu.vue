<template>
  <a-card title="系统菜单">
    <div style="display: flex">
      <a-input-search style="margin-right: 200px;margin-bottom: 8px" placeholder="搜索" @change="onChange"/>
      <a-button type="primary" style="margin-bottom: 8px" @click="editMenu({})">新增</a-button>
      <a-button type="primary" style="margin-bottom: 8px" @click="deleteMenu(checkedKeys)">删除</a-button>
    </div>
    <div style="margin-bottom: 8px">
      <a type="primary" @click="toggleExpandAll">{{ expandAll ? '折叠所有' : '展开所有' }}</a>
    </div>
    <div>
      <a-table
        :scroll="{ x: 'max-content' }"
        v-if="menuTree.length > 0"
        style="width: 100%;"
        :data-source="menuTree"
        :columns="columns"
        :pagination="false"
        :expandable="expandable"
        :expanded-row-keys.sync="expandedKeys"
        :row-selection="rowSelection">
        <template v-slot:menuId="menuId">
          <span style="font-weight: bold">{{ menuId }}</span>
        </template>
        <template v-slot:customTitle="customTitle">
          <span v-if="customTitle.indexOf(searchValue) > -1" style="font-weight: bold">
            {{ customTitle.substr(0, customTitle.indexOf(searchValue)) }}
            <span style="color: #f50">{{ searchValue }}</span>
            {{ customTitle.substr(customTitle.indexOf(searchValue) + searchValue.length) }}
          </span>
          <span v-else>{{ customTitle }}</span>
        </template>
        <template v-slot:icon="icon">
          <img v-if="currentIcon(icon)" :src="icon" alt="icon" style="width: 16px; margin-right: 8px;" />
          <a-icon v-else :component="icon" />
        </template>
        <template v-slot:isCache="isCache">
          <span v-if="isCache === '0'">是</span>
          <span v-else>否</span>
        </template>
        <template v-slot:menuType="menuType">
          <span v-if="menuType === 'M'">目录</span>
          <span v-else-if="menuType === 'C'">菜单</span>
          <span v-else>按钮</span>
        </template>
        <template v-slot:action="record">
          <a-button type="primary" size="small" @click="editMenu(record)">编辑</a-button>
          <a-button type="danger" size="small" @click="deleteMenu(record)">删除</a-button>
        </template>
      </a-table>
    </div>
    <EditMenu @editMenuSuccess="getSystemMenu()" v-if="visible" :visible.sync="visible" :menu="menu" :menu-tree="menuTree"/>
  </a-card>
</template>

<script>
import { deleteMenu, getMenuList } from '@/api/system/menu'
import { icons } from '@/core/icons'
import EditMenu from '@/views/system/menu/editMenu.vue'

export default {
  name: 'SystemMenu',
  components: { EditMenu },
  data () {
    return {
      menuList: [],
      menuTree: [],
      expandAll: false,
      checkedKeys: [],
      searchValue: '',
      expandedKeys: [],
      visible: false,
      menu: {},
      columns: [
        {
          title: '序号ID',
          dataIndex: 'menuId',
          key: 'menuId',
          width: '10%',
          scopedSlots: { customRender: 'menuId' }
        },
        {
          title: '菜单名称',
          dataIndex: 'title',
          key: 'title',
          scopedSlots: { customRender: 'customTitle' }
        },
        {
          title: '组件',
          dataIndex: 'component',
          key: 'component'
        },
        {
          title: '路由名称',
          dataIndex: 'routeName',
          key: 'routeName'
        },
        {
          title: '路径',
          dataIndex: 'path',
          key: 'path'
        },
        {
          title: '权限标识',
          dataIndex: 'perms',
          key: 'perms'
        },
        {
          title: '父菜单ID',
          dataIndex: 'parentId',
          key: 'parentId'
        },
        {
          title: '图标',
          dataIndex: 'icon',
          key: 'icon',
          scopedSlots: { customRender: 'icon' }
        },
        {
          title: '是否缓存',
          dataIndex: 'isCache',
          key: 'isCache',
          scopedSlots: { customRender: 'isCache' }
        },
        {
          title: '类型',
          dataIndex: 'menuType',
          key: 'menuType',
          scopedSlots: { customRender: 'menuType' }
        },
        {
          title: '排序',
          dataIndex: 'orderNum',
          key: 'orderNum'
        },
        {
          title: '操作', // 编辑列
          key: 'action',
          width: '10%',
          scopedSlots: { customRender: 'action' }
        }
      ]
    }
  },
  methods: {
    toggleExpandAll () {
      this.expandAll = !this.expandAll
      if (this.expandAll) {
        this.expandedKeys = this.menuList.map(menu => menu.menuId)
      } else {
        this.expandedKeys = ['1']
      }
    },
    getSystemMenu () {
      getMenuList().then(res => {
        this.menuList = res.result.menus.map(menu => {
          menu.title = this.$t(menu.menuName)
          menu.key = menu.menuId
          return menu
        })
        this.menuTree = this.convertMenusToTreeData(res.result.menus)
      })
    },
    onChange (e) {
      const value = e.target.value
      const expandedKeys = this.menuList
        .map(item => {
          if (item.title.indexOf(value) > -1) {
            return this.getParentKey(item.key, this.menuTree)
          }
          return null
        })
        .filter((item, i, self) => item && self.indexOf(item) === i)
      expandedKeys.push('1')
      Object.assign(this, {
        expandedKeys,
        searchValue: value
      })
    },
    // 将菜单数据转换为树形结构
    convertMenusToTreeData (menus) {
      this.expandedKeys = ['1']
      const map = new Map()
      const treeData = []

      menus.forEach((menu) => {
        const node = {
          menuId: menu.menuId, // 菜单 ID
          title: this.$t(menu.menuName), // 菜单名称
          value: menu.menuId, // 唯一标识
          key: menu.menuId, // 同样用 menuId 做 key
          children: null, // 初始化子节点
          component: menu.component,
          isCache: menu.isCache,
          routeName: menu.routeName,
          perms: menu.perms,
          path: menu.path,
          parentId: menu.parentId,
          menuType: menu.menuType,
          icon: icons[menu.icon] || icons['logo'],
          orderNum: menu.orderNum
        }
        map.set(menu.menuId, node)

        if (menu.parentId === '0') {
          // 根节点直接放入 treeData
          node.isParent = true
          treeData.push(node)
        } else {
          // 否则将其放入父节点的 children 中
          const parentNode = map.get(menu.parentId)
          if (parentNode) {
            parentNode.children = parentNode.children || []
            parentNode.children.push(node)
          }
        }
      })
      return treeData
    },
    getParentKey (key, tree) {
      let parentKey
      for (let i = 0; i < tree.length; i++) {
        const node = tree[i]
        if (node.children) {
          if (node.children.some(item => item.key === key)) {
            parentKey = node.key
          } else if (this.getParentKey(key, node.children)) {
            parentKey = this.getParentKey(key, node.children)
          }
        }
      }
      return parentKey
    },
    expandable (row) {
      return row.children && row.children.length > 0
    },
    editMenu (record) {
      this.menu = this.menuList.find(menu => menu.menuId === record.key) || {}
      this.visible = true
    },
    deleteMenu (record) {
      record = record instanceof Array ? record : [record.menuId]
      this.$confirm({
        title: '提示',
        content: record.length > 1 ? '确定删除选中的菜单吗？' : '确定删除该菜单吗？',
        onOk: () => {
          deleteMenu(record).then(res => {
            this.getSystemMenu()
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
    changeSelectedRowKeys (checkedKeys) {
      this.checkedKeys = checkedKeys
    },
    currentIcon (icon) {
      return typeof icon === 'string'
    }
  },
  mounted () {
    this.getSystemMenu()
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
  },
  watch: {
    searchValue (val) {
      if (!val) {
        this.expandedKeys = ['1']
      }
    }
  }
}
</script>

<style scoped lang="less">
.ant-btn {
  margin-right: 10px;
}
</style>
