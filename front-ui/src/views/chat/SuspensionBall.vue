<template>
  <div>
    <div class="sssss">
      <div
        class="callback float"
        @click="clickDB()"
        @mousedown="down"
        @touchstart="down"
        @mousemove="move"
        @touchmove="move"
        @mouseover="over"
        @mouseout="out"
        @mouseup="end"
        @touchend="end"
        ref="refDB"
      >
        <span>{{ name }}</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'SuspensionBall',
  props: {
    name: {
      type: String,
      default: ''
    }
  },

  data () {
    return {
      left: 0,
      top: 40,
      bg: 1,
      menu: false,
      isLoading: false,
      // 控制使用
      flags: false,
      position: {
        x: 0,
        y: 0
      },
      nx: '',
      ny: '',
      dx: '',
      dy: '',
      xPum: '',
      yPum: '',
      movb: 1,
      num: 1
    }
  },
  mounted () {
    this.left = this.$refs.refDB.offsetLeft - 750
  },
  methods: {
    out2 () {
      this.menu = false
    },
    over2 () { },
    out () {
      this.bg = 2
    },
    over () {
      this.menu = true
      this.num = 2
      this.bg = 1
    },
    callback () {
      this.$router.go(-1)
    },
    onRefresh () {
      // window.location.reload();
      setTimeout((res) => {
        console.log(res)
        this.isLoading = false
      }, 1000)
    },
    down () {
      this.flags = true
      var touch
      if (event.touches) {
        touch = event.touches[0]
      } else {
        touch = event
      }
      this.position.x = touch.clientX
      this.position.y = touch.clientY
      this.dx = this.$refs.refDB.offsetLeft
      this.dy = this.$refs.refDB.offsetTop
    },
    move () {
      if (this.flags) {
        this.movb = 2
        this.menu = false
        var touch
        if (event.touches) {
          touch = event.touches[0]
        } else {
          touch = event
        }
        this.nx = touch.clientX - this.position.x
        this.ny = touch.clientY - this.position.y
        this.xPum = this.dx + this.nx
        this.yPum = this.dy + this.ny
        // 屏幕宽度减去自身控件宽度
        const width = window.innerWidth - this.$refs.refDB.offsetWidth
        // 屏幕高度减去自身控件高度
        const height = window.innerHeight - this.$refs.refDB.offsetHeight
        this.xPum < 0 && (this.xPum = 0)
        this.yPum < 0 && (this.yPum = 0)
        this.xPum > width && (this.xPum = width)
        this.yPum > height && (this.yPum = height)
        // if (this.xPum >= 0 && this.yPum >= 0 && this.xPum<= width &&this.yPum<= height) {
        this.$refs.refDB.style.left = this.xPum + 'px'
        this.$refs.refDB.style.top = this.yPum + 'px'
        this.left = this.xPum - 750
        this.top = this.yPum
        // }
        // 阻止页面的滑动默认事件
        document.addEventListener(
          'touchmove',
          function () {
            event.preventDefault()
          },
          false
        )
      }
    },
    // 鼠标释放时候的函数
    end () {
      this.flags = false
    },
    clickDB () {
      this.$emit('clickDB')
    }
  }
}
</script>
<style scoped>
.callback {
  position: fixed;
  width: 90px;
  height: 90px;
  background-repeat: no-repeat;
  background-size: 100% 100%;
  background-image: url("../assets/db.png") !important;
  top: 20%;
  right: 1%;
  z-index: 99999;
}

.float {
  cursor: pointer;
  position: fixed;
  touch-action: none;
  text-align: center;
  border-radius: 50%;
  line-height: 40px;
  color: white;
}

.menuclass {
  text-align: left;
  position: absolute;
  color: #000;
  width: 764px;
  background: #ffffff;
  box-shadow: 0px 6px 26px 1px rgba(51, 51, 51, 0.16);
  padding: 20px;
}

.sssss {
  position: relative;
  background-color: #000;
  right: 0;
  z-index: 99999;
}

.titlea {
  font-size: 18px;
  font-family: Microsoft YaHei-Bold, Microsoft YaHei;
  font-weight: bold;
  color: #333333;
}

.boxa {
  display: flex;
  flex-wrap: wrap;
  margin-top: 20px;
  z-index: 999999;
}

.item {
  width: 168px;
  height: 75px;
  border-radius: 4px 4px 4px 4px;
  font-size: 16px;
  font-family: Microsoft YaHei-Bold, Microsoft YaHei;
  font-weight: bold;
  color: #ffffff;
  text-align: center;
  margin-left: 7px;
  line-height: 75px;
}
</style>
