<template>
  <div class="account-settings-info-view">
    <a-row :gutter="16" type="flex" justify="center">
      <a-col :order="isMobile ? 2 : 1" :md="24" :lg="16">
        <a-form layout="vertical" :form="form">
          <a-form-item
            :label="$t('account.settings.basic.nickname')"
            :required="true"
          >
            <a-input
              type="text"
              :placeholder="$t('account.settings.basic.nickname-message')"
              v-decorator="['nickName']" />
          </a-form-item>
          <a-form-item
            :label="$t('account.settings.basic.profile')"
          >
            <a-textarea rows="4" :placeholder="$t('account.settings.basic.profile-message')" v-decorator="['remark']" />
          </a-form-item>

          <a-form-item
            :label="$t('account.settings.basic.email')"
            :required="true"
          >
            <a-input type="email" :placeholder="$t('account.settings.basic.email-message')" v-decorator="['email']" />
          </a-form-item>

          <a-form-item>
            <a-button type="primary" @click="submitUserInfo">{{ $t('account.settings.basic.update') }}</a-button>
          </a-form-item>
        </a-form>

      </a-col>
      <a-col :order="1" :md="24" :lg="8" :style="{ minHeight: '180px' }">
        <div class="ant-upload-preview" @click="$refs.modal.edit(1)" >
          <a-icon type="cloud-upload-o" class="upload-icon"/>
          <div class="mask">
            <a-icon type="plus" />
          </div>
          <img :src="option.img"/>
        </div>
      </a-col>

    </a-row>

    <avatar-modal ref="modal" @ok="setavatar"/>

  </div>
</template>

<script>
import AvatarModal from './AvatarModal'
import { baseMixin } from '@/store/app-mixin'
import { getUserInfo, updateUserInfo } from '@/api/user'
import storage from 'store'
import { AVATAR } from '@/store/mutation-types'

export default {
  mixins: [baseMixin],
  components: {
    AvatarModal
  },
  data () {
    return {
      form: this.$form.createForm(this),
      preview: {},
      option: {
        img: ''
      }
    }
  },
  methods: {
    setavatar (data) {
      this.option.img = URL.createObjectURL(data)
    },
    getUserInfo () {
      getUserInfo().then(res => {
        res = res.result
        this.option.img = storage.get(AVATAR)
        this.form.setFieldsValue({
          nickName: res.user.nickName,
          email: res.user.email,
          remark: res.user.remark
        })
      })
    },
    submitUserInfo () {
      // 表单校验
      this.form.validateFields((err, values) => {
        if (!err) {
          values.userId = this.$store.getters.userInfo.userId
          updateUserInfo(values).then(res => {
            this.$message.success('更新成功')
          }).catch(() => {
            this.$message.error('更新失败')
          })
        }
      })
    }
  },
  computed: {
  },
  mounted () {
    this.getUserInfo()
  }
}
</script>

<style lang="less" scoped>

  .avatar-upload-wrapper {
    height: 200px;
    width: 100%;
  }

  .ant-upload-preview {
    position: relative;
    margin: 0 auto;
    width: 100%;
    max-width: 180px;
    height: 180px;
    border-radius: 50%;
    box-shadow: 0 0 4px #ccc;

    .upload-icon {
      position: absolute;
      top: 0;
      right: 10px;
      font-size: 1.4rem;
      padding: 0.5rem;
      background: rgba(222, 221, 221, 0.7);
      border-radius: 50%;
      border: 1px solid rgba(0, 0, 0, 0.2);
    }
    .mask {
      opacity: 0;
      position: absolute;
      background: rgba(0,0,0,0.4);
      cursor: pointer;
      transition: opacity 0.4s;

      &:hover {
        opacity: 1;
      }

      i {
        font-size: 2rem;
        position: absolute;
        top: 50%;
        left: 50%;
        margin-left: -1rem;
        margin-top: -1rem;
        color: #d6d6d6;
      }
    }

    img, .mask {
      width: 100%;
      max-width: 180px;
      height: 100%;
      border-radius: 50%;
      overflow: hidden;
    }
  }
</style>
