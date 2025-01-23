<template>
  <a-modal
    title="编辑菜单"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @cancel="handleCancel"
    @ok="handleOk"
  >
    <a-form :form="form" layout="vertical" style=".ant-form-item{margin-bottom: 5px}">
      <a-form-item label="菜单名称">
        <a-input
          v-decorator="[ 'menuName', { initialValue: menu.menuName , rules: [ { required: true, message: '请输入菜单名称' } ] } ]" />
      </a-form-item>
      <a-form-item label="组件">
        <a-input
          v-decorator="[ 'component', { initialValue: menu.component, rules: [ { required: true, message: '请输入组件' } ] } ]" />
      </a-form-item>
      <a-form-item label="路由名称">
        <a-input
          v-decorator="[ 'routeName', { initialValue: menu.routeName, rules: [ { required: true, message: '请输入路由名称' } ] } ]" />
      </a-form-item>
      <a-form-item label="路径">
        <a-input
          v-decorator="[ 'path', { initialValue: menu.path,rules: [ { required: true, message: '请输入路径' } ] } ]" />
      </a-form-item>
      <a-form-item label="权限标识">
        <a-input
          v-decorator="[ 'perms', { initialValue: menu.perms,rules: [ { required: true, message: '请输入权限标识' } ] } ]" />
      </a-form-item>
      <a-form-item label="父级菜单">
        <a-tree-select
          :dropdown-style="{ maxHeight: '400px', overflow: 'auto' }"
          v-decorator="[ 'parentId', { initialValue: menu.parentId,rules: [ { required: true, message: '请选择父级菜单' } ] } ]"
          :treeData="menuTree"
          placeholder="请选择父级菜单"
          @select="onSelect"
          allowClear
        />
      </a-form-item>
      <a-form-item label="图标">
        <div style="display: flex">
          <div style="flex: 1">
            <a-select
              v-decorator="[ 'icon', { initialValue: menu.icon,rules: [ { required: true, message: '请选择图标' } ] } ]"
              placeholder="请选择图标"
              allow-clear
            >
              <a-select-option
                v-for="icon in Object.entries(icons)"
                :key="icon[0]"
                :value="icon[0]">
                <img v-if="currentIcon(icon[1])" :src="icon[1]" alt="icon" style="width: 16px; margin-right: 8px;" />
                <a-icon v-else :component="icon[1]" style="margin-right: 8px;" />
                {{ icon[0] }}
              </a-select-option>
            </a-select>
          </div>
        </div>
      </a-form-item>

      <a-form-item label="是否缓存">
        <a-radio-group
          v-decorator="[ 'isCache', { initialValue: menu.isCache || '1',rules: [ { required: true, message: '请选择是否缓存' } ] } ]">
          <a-radio value="1">是</a-radio>
          <a-radio value="0">否</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="菜单类型">
        <a-radio-group
          v-decorator="[ 'menuType', { initialValue: menu.menuType || 'M',rules: [ { required: true, message: '请选择菜单类型' } ] } ]">
          <a-radio value="M">目录</a-radio>
          <a-radio value="C">菜单</a-radio>
          <a-radio value="F">按钮</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="排序">
        <a-input
          v-decorator="[ 'orderNum', { initialValue: menu.orderNum,rules: [ { required: true, message: '请输入排序' } ] } ]" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
import { icons } from '@/core/icons'
import { createMenu, updateMenu } from '@/api/system/menu'

export default {
  name: 'EditMenu',
  computed: {
    icons () {
      return icons
    }
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    menu: {
      type: Object,
      default: () => ({})
    },
    menuTree: {
      type: Array,
      default: () => []
    }
  },
  data () {
    return {
      form: this.$form.createForm(this),
      confirmLoading: false
    }
  },
  methods: {
    currentIcon (icon) {
      return typeof icon === 'string'
    },
    handleCancel () {
      this.$emit('update:visible', false)
    },
    handleOk () {
      this.confirmLoading = true
      const { form: { validateFields } } = this
      validateFields((err, values) => {
        if (!err) {
          values.menuId = this.menu.menuId
          if (this.menu.menuId) {
            updateMenu(values).then((res) => {
              this.confirmLoading = false
              if (res.status === '0') {
                this.$message.success('编辑成功')
                this.$emit('editMenuSuccess')
                this.$emit('update:visible', false)
              } else {
                this.$message.error(res.errstr)
              }
            }).catch((err) => {
              this.confirmLoading = false
              this.$message.error(err)
            })
          } else {
            createMenu(values).then((res) => {
              this.confirmLoading = false
              if (res.status === '0') {
                this.$message.success('新增成功')
                this.$emit('editMenuSuccess')
                this.$emit('update:visible', false)
              } else {
                this.$message.error(res.errstr)
              }
            }).catch((err) => {
              this.confirmLoading = false
              this.$message.error(err)
            })
          }
        } else {
          this.confirmLoading = false
        }
      })
    },
    onSelect (value, node, extra) {
      if (value === this.menu.menuId) {
        this.$message.error('不能选择自己作为父级菜单')
        this.$nextTick(() => {
          this.form.setFieldsValue({ parentId: this.menu.parentId })
        })
      }
    }
  },
  mounted () {
    this.form.getFieldDecorator('icon', { initialValue: this.menu.iconPath || 'logon' })
  }
}
</script>

<style scoped>

</style>
