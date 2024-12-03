<template>
  <transition>
    <div
      ref="floatBall"
      class="floatBall"
      @touchstart.stop="handleStart"
      @touchmove.prevent.stop="handleMove($event)"
      @touchend.stop="handleEnd"
      :style="{ left: left + 'px', top: top + 'px', width: itemWidth + 'px', height: itemHeight + 'px' }"
      v-if="isShow">
      {{ text }}
    </div>
  </transition>
</template>

<script>
export default {
  name: 'ChatBall',
  props: {
    text: {
      type: String,
      default: '悬浮'
    },
    itemWidth: {
      type: Number,
      default: 80
    },
    itemHeight: {
      type: Number,
      default: 80
    }
  },
  data () {
    return {
      left: 0,
      top: 0,
      startToMove: false,
      isShow: true,
      timer: null,
      currentTop: null,
      clientW: document.documentElement.clientWidth, // 视口宽
      clientH: document.documentElement.clientHeight // 视口高
    }
  },
  created () {
    this.left = (this.clientW - this.itemWidth - 30)
    this.top = (this.clientH / 2 - this.itemHeight / 2)
  },
  mounted () {
    this.bindScrollEvent()
  },
  beforeDestroy () {
    this.removeScrollEvent()
  },
  methods: {
    handleStart () {
      this.startToMove = true
      this.$refs.floatBall.style.transition = 'none'
    },
    handleMove (e) {
      const clientX = e.targetTouches[0].clientX
      const clientY = e.targetTouches[0].clientY
      const isInScreen = clientX <= this.clientW && clientX >= 0 && clientY <= this.clientH && clientY >= 0
      if (this.startToMove && e.targetTouches.length === 1) {
        if (isInScreen) {
          this.left = clientX - this.itemWidth / 2
          this.top = clientY - this.itemHeight / 2
        }
      }
    },
    handleEnd () {
      if (this.left < (this.clientW / 2)) {
        // 不让贴边 所以设置30没设置0
        this.left = 30
        this.handleIconY()
      } else {
        // 不让贴边 所以减30
        this.left = this.clientW - this.itemWidth - 30
        this.handleIconY()
      }
      this.$refs.floatBall.style.transition = 'all .3s'
    },
    handleIconY () {
      if (this.top < 0) {
        this.top = 30
      } else if (this.top + this.itemHeight > this.clientH) {
        this.top = this.clientH - this.itemHeight - 30
      }
    },
    bindScrollEvent () {
      window.addEventListener('scroll', this.handleScrollStart)
    },
    removeScrollEvent () {
      window.removeEventListener('scroll', this.handleScrollStart)
    },
    handleScrollStart () {
      this.isShow = false
      this.timer && clearTimeout(this.timer)
      this.timer = setTimeout(() => {
        this.handleScrollEnd()
      }, 300)
      this.currentTop = document.documentElement.scrollTop || document.body.scrollTop
    },
    handleScrollEnd () {
      this.scrollTop = document.documentElement.scrollTop || document.body.scrollTop
      // 判断是否停止滚动的条件
      if (this.scrollTop === this.currentTop) {
        this.isShow = true
      }
    }
  }
}
</script>

<style scoped>
.floatBall {
    position: fixed;
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background-color: #f0f;
    line-height: 80px;
    text-align: center;
    color: #fff;
}

.v-enter {
    opacity: 1;
}

.v-leave-to {
    opacity: 0;
}

.v-enter-active,
.v-leave-active {
    transition: opacity 0.3s;
}
</style>
