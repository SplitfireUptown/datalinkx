<template>
  <a-modal
    title="编辑角色"
    :width="800"
    :visible="visible"
    :confirmLoading="confirmLoading"
    @cancel="handleCancel"
    @ok="handleOk">
    <a-form :form="form" layout="vertical" style=".ant-form-item{margin-bottom: 5px}">
      <a-form-item label="角色名称" prop="roleName">
        <a-input v-decorator="[ 'roleName', { initialValue: role.roleName , rules: [ { required: true, message: '请输入角色名称' } ] } ]" />
      </a-form-item>
      <a-form-item label="角色标识" prop="roleKey">
        <a-input v-decorator="[ 'roleKey', { initialValue: role.roleKey , rules: [ { required: true, message: '请输入角色标识' } ] } ]" />
      </a-form-item>
      <a-form-item label="显示顺序" prop="roleSort">
        <a-input-number v-decorator="[ 'roleSort', { initialValue: role.roleSort, rules: [ { required: true, message: '请输入显示顺序' } ] } ]" />
      </a-form-item>
      <a-form-item label="角色状态" prop="status">
        <a-radio-group v-decorator="[ 'status', { initialValue: role.status || '0', rules: [ { required: true, message: '请选择角色状态' } ] } ]">
          <a-radio value="0">正常</a-radio>
          <a-radio value="1">停用</a-radio>
        </a-radio-group>
      </a-form-item>
      <a-form-item label="备注" prop="remark">
        <a-textarea v-decorator="[ 'remark', { initialValue: role.remark } ]" />
      </a-form-item>
    </a-form>
  </a-modal>
</template>
<script>
import { createRole, updateRole } from '@/api/system/role'

export default {
  name: 'EditRole',
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    role: {
      type: Object,
      default: () => ({})
    }
  },
  data () {
    return {
      form: this.$form.createForm(this),
      confirmLoading: false
    }
  },
  mounted () {
  },
  methods: {
    handleCancel () {
      this.$emit('update:visible', false)
    },
    handleOk () {
      this.confirmLoading = true
      const { form: { validateFields } } = this
      validateFields((err, values) => {
        if (!err) {
          values.roleId = this.role.roleId
          if (this.role.roleId) {
            updateRole(values).then((res) => {
              this.confirmLoading = false
              if (res.status === '0') {
                this.$message.success('编辑成功')
                this.$emit('editRoleSuccess')
                this.$emit('update:visible', false)
              } else {
                this.$message.error(res.errstr)
              }
            }).catch((err) => {
              this.confirmLoading = false
              this.$message.error(err)
            })
          } else {
            createRole(values).then((res) => {
              this.confirmLoading = false
              if (res.status === '0') {
                this.$message.success('新增成功')
                this.$emit('editRoleSuccess')
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
    }
  }
}

</script>
<style scoped lang="less">

</style>
