<template>
  <a-modal
    title="编辑菜单"
    :width="800"
    :visible="visible"
    @cancel="handleCancel"
    @ok="handleOk"
  >
    <a-form :form="form" layout="vertical">
      <a-form-item label="菜单名称">
        <a-input v-decorator="[ 'title', { initialValue: menu.title } ]" />
      </a-form-item>
      <a-form-item label="组件">
        <a-input v-decorator="[ 'component', { initialValue: menu.component } ]" />
      </a-form-item>
      <a-form-item label="路由名称">
        <a-input v-decorator="[ 'routeName', { initialValue: menu.routeName } ]" />
      </a-form-item>
      <a-form-item label="路径">
        <a-input v-decorator="[ 'path', { initialValue: menu.path } ]" />
      </a-form-item>
      <a-form-item label="图标" v-if="!iconEdit">
        <div style="display: flex">
          <div style="flex: 1">
            <a-input>
              <img slot="prefix" v-if="currentIcon(menu.icon)" :src="menu.icon" alt="icon" style="width: 16px; margin-right: 8px;" />
              <a-icon slot="prefix" v-else :component="menu.icon" style="margin-right: 8px;" />
            </a-input>
          </div>
          <div style="align-content: center;width: 20%;text-align: end">
            <a v-if="!iconEdit" @click="iconEdit = true">编辑图标</a>
          </div>
        </div>
      </a-form-item>
      <a-form-item label="图标" v-else>
        <div style="display: flex">
          <div style="flex: 1">
            <a-select
              v-decorator="[ 'icon' ]"
              placeholder="请选择图标"
              allow-clear
            >
              <a-select-option
                v-for="(icon, key) in Object.entries(icons)"
                :key="key"
                :value="key">
                <img v-if="currentIcon(icon[1])" :src="icon[1]" alt="icon" style="width: 16px; margin-right: 8px;" />
                <a-icon v-else :component="icon[1]" style="margin-right: 8px;" />
              </a-select-option>
            </a-select>
          </div>
          <div style="align-content: center;width: 20%;text-align: end">
            <a v-if="iconEdit" @click="iconEdit = false">取消</a>
          </div>
        </div>
      </a-form-item>

      <a-form-item label="是否缓存">
        <a-radio-group v-decorator="[ 'isCache', { initialValue: menu.isCache } ]">
          <a-radio value="1">是</a-radio>
          <a-radio value="0">否</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="排序">
        <a-input v-decorator="[ 'orderNum', { initialValue: menu.orderNum } ]" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script>
import { icons } from '@/core/icons'

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
    }
  },
  data () {
    return {
      form: this.$form.createForm(this),
      iconEdit: false
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
      this.$emit('update:visible', false)
    }
  },
  mounted () {
  }
}
</script>

<style scoped>

</style>
