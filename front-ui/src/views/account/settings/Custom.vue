<template>
  <a-list itemLayout="horizontal">
    <a-list-item>
      <a-list-item-meta>
        <template v-slot:title>
          <a>风格配色</a>
        </template>
        <template v-slot:description>
          <span>
            整体风格配色设置
          </span>
        </template>
      </a-list-item-meta>
      <template v-slot:actions>
        <a-switch checkedChildren="暗色" unCheckedChildren="白色" :defaultChecked="navTheme === 'dark' && true || false" @change="onChange" />
      </template>
    </a-list-item>
    <a-list-item>
      <a-list-item-meta>
        <template v-slot:title>
          <a>主题色</a>
        </template>
        <template v-slot:description>
          <span>
            页面风格配色： <a>{{ colorFilter(primaryColor) }}</a>
          </span>
          <div style="height: 28px">
            <a-tooltip class="setting-drawer-theme-color-colorBlock" v-for="(item, index) in colorList()" :key="index">
              <template slot="title">
                {{ item.key }}
              </template>
              <a-tag :color="item.color" @click="changeColor(item.color)">
                <a-icon type="check" v-if="item.color === primaryColor"></a-icon>
              </a-tag>
            </a-tooltip>
          </div>
        </template>
      </a-list-item-meta>
    </a-list-item>
  </a-list>
</template>
<script>
import { colorList, updateTheme } from '@/components/SettingDrawer/settingConfig'
import { baseMixin } from '@/store/app-mixin'
import { NAV_THEME, TOGGLE_COLOR, TOGGLE_NAV_THEME } from '@/store/mutation-types'

const themeMap = {
  'dark': '暗色',
  'light': '白色'
}

export default {
  mixins: [baseMixin],
  data () {
    return {
    }
  },
  filters: {
    themeFilter (theme) {
      return themeMap[theme]
    }
  },
  methods: {
    colorList () {
      return colorList
    },
    colorFilter (color) {
      // 转大写
      color = color.toUpperCase()
      colorList.map(o => {
        o.color = o.color.toUpperCase()
        return o
      })
      const c = colorList.find(o => o.color === color)
      return c && c.key
    },

    onChange (checked) {
      if (checked) {
        this.$store.commit(TOGGLE_NAV_THEME, NAV_THEME.DARK)
      } else {
        this.$store.commit(TOGGLE_NAV_THEME, NAV_THEME.LIGHT)
      }
    //   刷新
      window.location.reload()
    },

    changeColor (color) {
      if (this.primaryColor !== color) {
        this.$store.commit(TOGGLE_COLOR, color)
        updateTheme(color)
      }
      //   刷新
      window.location.reload()
    }
  }
}
</script>
<style lang="less" scoped>
  .setting-drawer-theme-color-colorBlock {
    width: 20px;
    height: 20px;
    border-radius: 2px;
    float: left;
    cursor: pointer;
    margin-right: 8px;
    margin-top: 8px;
    padding-left: 0px;
    padding-right: 0px;
    text-align: center;
    color: #fff;
    font-weight: 700;

    i {
      font-size: 14px;
    }
  }
</style>
