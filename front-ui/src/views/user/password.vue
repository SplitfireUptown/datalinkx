<template>
  <div>
    <a-popover
      placement="rightTop"
      :trigger="['focus']"
      :getPopupContainer="(trigger) => trigger.parentElement"
      v-model="state.passwordLevelChecked">
      <template slot="content">
        <div :style="{ width: '240px' }" >
          <div :class="['user-register', passwordLevelClass]">{{ $t(passwordLevelName) }}</div>
          <a-progress :percent="state.percent" :showInfo="false" :strokeColor=" passwordLevelColor " />
          <div style="margin-top: 10px;">
            <span>{{ $t('user.register.password.popover-message') }}
            </span>
          </div>
        </div>
      </template>
      <a-form-item>
        <a-input-password
          size="large"
          @click="handlePasswordInputClick"
          :placeholder="$t('user.register.password.placeholder')"
          v-decorator="['password', {rules: [{ required: true, message: $t('user.password.required') }, { validator: this.handlePasswordLevel }], validateTrigger: ['change', 'blur']}]"
        ></a-input-password>
      </a-form-item>
    </a-popover>
    <a-form-item>
      <a-input-password
        ref="password2"
        size="large"
        :placeholder="$t('user.register.confirm-password.placeholder')"
        v-decorator="['password2', {rules: [{ required: true, message: $t('user.password.required') }, { validator: this.handlePasswordCheck }], validateTrigger: ['change', 'blur']}]"
      ></a-input-password>
    </a-form-item>
  </div>
</template>
<script>
import { deviceMixin } from '@/store/device-mixin'
import { scorePassword } from '@/utils/util'

const levelNames = {
  0: 'user.password.strength.short',
  1: 'user.password.strength.low',
  2: 'user.password.strength.medium',
  3: 'user.password.strength.strong'
}
const levelClass = {
  0: 'error',
  1: 'error',
  2: 'warning',
  3: 'success'
}
const levelColor = {
  0: '#ff0000',
  1: '#ff0000',
  2: '#ff7e05',
  3: '#52c41a'
}
export default {
  name: 'Password',
  mixins: [deviceMixin],
  props: {
    state: {
      type: Object,
      default: () => {
        return {
          time: 60,
          level: 0,
          smsSendBtn: false,
          passwordLevel: 0,
          passwordLevelChecked: false,
          percent: 10,
          progressColor: '#FF0000'
        }
      }
    }
  },
  data () {
    return {

    }
  },
  mounted () {
  },
  methods: {
    handlePasswordCheck (rule, value, callback) {
      const password = this.$refs.password2.value
      if (value === undefined) {
        callback(new Error(this.$t('user.password.required')))
      }
      if (value && password && value.trim() !== password.trim()) {
        callback(new Error(this.$t('user.password.twice.msg')))
      }
      callback()
    },
    handlePasswordInputClick () {
      if (!this.isMobile) {
        this.state.passwordLevelChecked = true
        return
      }
      this.state.passwordLevelChecked = false
    },
    handlePasswordLevel (rule, value, callback) {
      if (!value) {
        return callback()
      }
      if (value.length >= 6) {
        if (scorePassword(value) >= 30) {
          this.state.level = 1
        }
        if (scorePassword(value) >= 60) {
          this.state.level = 2
        }
        if (scorePassword(value) >= 80) {
          this.state.level = 3
        }
      } else {
        this.state.level = 0
        callback(new Error(this.$t('user.password.strength.msg')))
      }
      this.state.passwordLevel = this.state.level
      this.state.percent = this.state.level * 33

      callback()
    }
  },
  computed: {
    passwordLevelClass () {
      return levelClass[this.state.passwordLevel]
    },
    passwordLevelName () {
      return levelNames[this.state.passwordLevel]
    },
    passwordLevelColor () {
      return levelColor[this.state.passwordLevel]
    }
  }
}
</script>
<style scoped lang="less">
.user-register {

  &.error {
    color: #ff0000;
  }

  &.warning {
    color: #ff7e05;
  }

  &.success {
    color: #52c41a;
  }

}

.user-layout-register {
  .ant-input-group-addon:first-child {
    background-color: #fff;
  }
}
</style>
