<template>
  <a-card title="系统菜单">
    <a-input-search style="margin-bottom: 8px" placeholder="搜索" @change="onChange"/>
    <div>
      <a-table
        v-if="menuTree.length > 0"
        style="width: 100%;"
        :data-source="menuTree"
        :columns="columns"
        :pagination="false"
        :expanded-row-keys.sync="expandedKeys"
        :row-selection="rowSelection">
        <template v-slot:customTitle="customTitle">
          <span v-if="customTitle.indexOf(searchValue) > -1" style="font-weight: bold">
            {{ customTitle.substr(0, customTitle.indexOf(searchValue)) }}
            <span style="color: #f50">{{ searchValue }}</span>
            {{ customTitle.substr(customTitle.indexOf(searchValue) + searchValue.length) }}
          </span>
          <span v-else>{{ customTitle }}</span>
        </template>
        <template v-slot:icon="icon">
          <a-icon :component="icon" />
        </template>
        <template v-slot:isCache="isCache">
          <span v-if="isCache === '1'">是</span>
          <span v-else>否</span>
        </template>
        <template v-slot:action="record">
          <a-button type="primary" size="small" @click="editMenu(record)">编辑</a-button>
          <a-button type="danger" size="small">删除</a-button>
        </template>
      </a-table>
    </div>
    <EditMenu v-if="visible" :visible.sync="visible" :menu="menu"/>
  </a-card>
</template>

<script>
import { getMenuList } from '@/api/system/menu'
import { icons } from '@/core/icons'
import EditMenu from '@/views/system/menu/editMenu.vue'

export default {
  name: 'SystemMenu',
  components: { EditMenu },
  data () {
    return {
      menuList: [],
      menuTree: [],
      checkedKeys: [],
      searchValue: '',
      expandedKeys: [],
      visible: false,
      menu: {},
      columns: [
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
      this.checkedKeys = menus.map(menu => menu.menuId)
      this.expandedKeys = ['1']
      const map = new Map()
      const treeData = []

      menus.forEach((menu) => {
        const node = {
          title: this.$t(menu.menuName), // 菜单名称
          value: menu.menuId, // 唯一标识
          key: menu.menuId, // 同样用 menuId 做 key
          children: [], // 初始化子节点为空数组
          component: menu.component,
          isCache: menu.isCache,
          routeName: menu.routeName,
          path: menu.path,
          icon: icons[menu.icon] || icons['logo'],
          orderNum: menu.orderNum,
          isParent: false
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
    editMenu (record) {
      this.menu = record
      this.visible = true
    },
    changeSelectedRowKeys (checkedKeys) {
      this.checkedKeys = checkedKeys
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

<style scoped>
.ant-btn{
  margin-right: 10px;
}
</style>
