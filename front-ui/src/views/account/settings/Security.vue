<template>
  <div>
    <a-list
      itemLayout="horizontal"
      :dataSource="data"
    >
      <a-list-item>
        <a-list-item-meta>
          <a slot="title">{{ passwordData.title }}</a>
          <span slot="description">
            <span class="security-list-description">{{ passwordData.description }}</span>
            <span class="security-list-value">{{ passwordData.value }}</span>
          </span>
        </a-list-item-meta>
        <template v-if="passwordData.actions">
          <a slot="actions" @click="passwordData.actions.callback">{{ passwordData.actions.title }}</a>
        </template>
      </a-list-item>
      <a-form :form="form" v-if="showAble">
        <Password ref="securityPassword" class="password" :state="state" />
        <a-form-item>
          <a-button
            size="large"
            type="primary"
            htmlType="submit"
            @click="passwordChange"
          >{{ $t('save') }}
          </a-button>
        </a-form-item>
      </a-form>
      <a-list-item v-for="(item,key) in data" :key="key">
        <a-list-item-meta>
          <a slot="title">{{ item.title }}</a>
          <span slot="description">
            <span class="security-list-description">{{ item.description }}</span>
            <span class="security-list-value">{{ item.value }}</span>
          </span>
        </a-list-item-meta>
        <template v-if="item.actions">
          <a slot="actions" @click="item.actions.callback">{{ item.actions.title }}</a>
        </template>
      </a-list-item>
    </a-list>
  </div>
</template>

<script>
import storage from 'store'
import { USER } from '@/store/mutation-types'
import Password from '@/views/user/password.vue'
import { updateUserPwd } from '@/api/user'
import { encrypt } from '@/utils/encrypt'

const levelNames = {
  0: 'user.password.strength.short',
  1: 'user.password.strength.low',
  2: 'user.password.strength.medium',
  3: 'user.password.strength.strong'
}
const passwordActionTitle = {
 false: 'account.settings.security.modify',
  true: 'close'
}
export default {
  name: 'Security',
  components: { Password },
  data () {
    return {
      form: this.$form.createForm(this),
      user: storage.get(USER),
      showAble: false,
      state: {
        time: 60,
        level: 0,
        smsSendBtn: false,
        passwordLevel: 0,
        passwordLevelChecked: false,
        percent: 10,
        progressColor: '#FF0000'
      },
      passwordAction: {
        title: this.$t('account.settings.security.modify'),
        callback: () => {
          this.chargeShowAble()
        }
      }
    }
  },
  methods: {
    chargeShowAble () {
      this.showAble = !this.showAble
      this.passwordAction.title = this.$t(passwordActionTitle[this.showAble])
    },
    passwordChange () {
      const { form: { validateFields } } = this
      validateFields({ force: true }, (err, values) => {
        if (!err) {
          encrypt(values.password).then(res => {
            values.password = res
            values.passwordLevel = this.$refs.securityPassword.state.passwordLevel
            values.userId = this.user.userId
            updateUserPwd(values).then((res) => {
              if (res.status !== '0') {
                this.$message.error(res.errstr)
              } else {
                this.$message.success(this.$t('save.ok'))
                this.$store.dispatch('Logout').then(() => {
                  this.$router.push({ name: 'login' })
                })
              }
            }).catch(err => {
              this.$message.error(err)
            })
          })
        }
      })
    }
  },
  mounted () {

  },
  computed: {
    passwordData () {
      return {
        title: this.$t('account.settings.security.password'),
        description: this.$t('account.settings.security.password-description'),
        value: this.$t(this.passwordLevelName).split('：')[1],
        actions: this.passwordAction
      }
    },
    data () {
        return [
        { title: this.$t('account.settings.security.phone'), description: this.$t('account.settings.security.phone-description'), value: '138****8293', actions: { title: this.$t('account.settings.security.modify'), callback: () => { this.$message.success('This is a message of success') } } },
        { title: this.$t('account.settings.security.question'), description: this.$t('account.settings.security.question-description'), value: '', actions: { title: this.$t('account.settings.security.set'), callback: () => { this.$message.error('This is a message of error') } } },
        { title: this.$t('account.settings.security.email'), description: this.$t('account.settings.security.email-description'), value: 'ant***sign.com', actions: { title: this.$t('account.settings.security.modify'), callback: () => { this.$message.warning('This is message of warning') } } },
        { title: this.$t('account.settings.security.mfa'), description: this.$t('account.settings.security.mfa-description'), value: '', actions: { title: this.$t('account.settings.security.bind'), callback: () => { this.$message.info('This is a normal message') } } }
      ]
    },
    passwordLevelName () {
      return levelNames[this.user.passwordLevel]
    }
  }
}
</script>

<style scoped>
.password{
  margin-top: 20px;
  width: 250px;
}
</style>
