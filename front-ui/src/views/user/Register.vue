<template>
  <div class="main user-layout-register">
    <h3><span>{{ $t('user.register.register') }}</span></h3>
    <a-form ref="formRegister" :form="form" id="formRegister">
      <a-form-item>
        <a-input
          size="large"
          type="text"
          :placeholder="$t('user.register.userName.placeholder')"
          v-decorator="['username', {rules: [{ required: true, message: $t('user.username.required') }], validateTrigger: ['change', 'blur']}]"
        ></a-input>
      </a-form-item>
      <a-form-item>
        <a-input
          size="large"
          type="text"
          :placeholder="$t('user.register.nickName.placeholder')"
          v-decorator="['nickname', {rules: [{ required: true, message: $t('user.nickname.required') }], validateTrigger: ['change', 'blur']}]"
        ></a-input>
      </a-form-item>
      <a-form-item>
        <a-input
          size="large"
          type="text"
          :placeholder="$t('user.register.email.placeholder')"
          v-decorator="['email', {rules: [{ required: true, type: 'email', message: $t('user.email.required') }], validateTrigger: ['change', 'blur']}]"
        ></a-input>
      </a-form-item>
      <Password :state="this.state" />
      <!--      <a-form-item>-->
      <!--        <a-input size="large" :placeholder="$t('user.login.mobile.placeholder')" v-decorator="['mobile', {rules: [{ required: true, message: $t('user.phone-number.required'), pattern: /^1[3456789]\d{9}$/ }, { validator: this.handlePhoneCheck } ], validateTrigger: ['change', 'blur'] }]">-->
      <!--          <a-select slot="addonBefore" size="large" defaultValue="+86">-->
      <!--            <a-select-option value="+86">+86</a-select-option>-->
      <!--            <a-select-option value="+87">+87</a-select-option>-->
      <!--          </a-select>-->
      <!--        </a-input>-->
      <!--      </a-form-item>-->
      <!--<a-input-group size="large" compact>
            <a-select style="width: 20%" size="large" defaultValue="+86">
              <a-select-option value="+86">+86</a-select-option>
              <a-select-option value="+87">+87</a-select-option>
            </a-select>
            <a-input style="width: 80%" size="large" placeholder="11 位手机号"></a-input>
          </a-input-group>-->

      <!--      <a-row :gutter="16">-->
      <!--        <a-col class="gutter-row" :span="16">-->
      <!--          <a-form-item>-->
      <!--            <a-input size="large" type="text" :placeholder="$t('user.login.mobile.verification-code.placeholder')" v-decorator="['captcha', {rules: [{ required: true, message: '请输入验证码' }], validateTrigger: 'blur'}]">-->
      <!--              <a-icon slot="prefix" type="mail" :style="{ color: 'rgba(0,0,0,.25)' }"/>-->
      <!--            </a-input>-->
      <!--          </a-form-item>-->
      <!--        </a-col>-->
      <!--        <a-col class="gutter-row" :span="8">-->
      <!--          <a-button-->
      <!--            class="getCaptcha"-->
      <!--            size="large"-->
      <!--            :disabled="state.smsSendBtn"-->
      <!--            @click.stop.prevent="getCaptcha"-->
      <!--            v-text="!state.smsSendBtn && $t('user.register.get-verification-code')||(state.time+' s')"></a-button>-->
      <!--        </a-col>-->
      <!--      </a-row>-->

      <a-form-item>
        <a-button
          size="large"
          type="primary"
          htmlType="submit"
          class="register-button"
          :loading="registerBtn"
          @click.stop.prevent="handleSubmit"
          :disabled="registerBtn">{{ $t('user.register.register') }}
        </a-button>
        <router-link class="login" :to="{ name: 'login' }">{{ $t('user.register.sign-in') }}</router-link>
      </a-form-item>

    </a-form>
  </div>
</template>

<script>
import { getSmsCaptcha, register } from '@/api/login'
import { deviceMixin } from '@/store/device-mixin'
import { encrypt } from '@/utils/encrypt'
import Password from '@/views/user/password.vue'
export default {
  name: 'Register',
  components: {
    Password
  },
  mixins: [deviceMixin],
  data () {
    return {
      form: this.$form.createForm(this),

      state: {
        time: 60,
        level: 0,
        smsSendBtn: false,
        passwordLevel: 0,
        passwordLevelChecked: false,
        percent: 10,
        progressColor: '#FF0000'
      },
      registerBtn: false
    }
  },
  methods: {
    handlePhoneCheck (rule, value, callback) {
      console.log('handlePhoneCheck, rule:', rule)
      console.log('handlePhoneCheck, value', value)
      console.log('handlePhoneCheck, callback', callback)

      callback()
    },
    handleSubmit () {
      const { form: { validateFields } } = this
      validateFields({ force: true }, (err, values) => {
        if (!err) {
          encrypt(values.password).then(res => {
                values.password = res
                values.passwordLevel = this.state.passwordLevel
                this.state.passwordLevelChecked = false
                register(values).then((res) => {
                  if (res.status !== '0') {
                    this.requestFailed(res)
                  } else {
                    this.$router.push({ name: 'registerResult', params: { ...values } })
                  }
                }).catch(err => {
                this.requestFailed(err)
                })
            }
          )
        }
      })
    },

    getCaptcha (e) {
      e.preventDefault()
      const { form: { validateFields }, state, $message, $notification } = this

      validateFields(['mobile'], { force: true },
        (err, values) => {
          if (!err) {
            state.smsSendBtn = true

            const interval = window.setInterval(() => {
              if (state.time-- <= 0) {
                state.time = 60
                state.smsSendBtn = false
                window.clearInterval(interval)
              }
            }, 1000)

            const hide = $message.loading('验证码发送中..', 0)

            getSmsCaptcha({ mobile: values.mobile }).then(res => {
              setTimeout(hide, 2500)
              $notification['success']({
                message: '提示',
                description: '验证码获取成功，您的验证码为：' + res.result.captcha,
                duration: 8
              })
            })`.catch(err => {
              setTimeout(hide, 1)
              clearInterval(interval)
              state.time = 60
              state.smsSendBtn = false
              this.requestFailed(err)
            })`
          }
        }
      )
    },
    requestFailed (err) {
      this.$notification['error']({
        message: '错误',
        description: this.$t(((err || {}).errstr || {}) || '请求出现错误，请稍后再试'),
        duration: 4
      })
      this.registerBtn = false
    }
  },
  watch: {
    'state.passwordLevel' (val) {
      console.log(val)
    }
  }
}
</script>
<style lang="less" scoped>
  .user-layout-register {

    & > h3 {
      font-size: 16px;
      margin-bottom: 20px;
    }

    .getCaptcha {
      display: block;
      width: 100%;
      height: 40px;
    }

    .register-button {
      width: 50%;
    }

    .login {
      float: right;
      line-height: 40px;
    }
  }
</style>
