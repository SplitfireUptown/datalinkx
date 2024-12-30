<template>
  <div class="base-loading-layer">
    <div :class="['size']" class="loader-outer">
      <div class="ant-spin-spinning">
        <span class="ant-spin-dot ant-spin-dot-spin">
          <i class="ant-spin-dot-item"></i>
          <i class="ant-spin-dot-item"></i>
          <i class="ant-spin-dot-item"></i>
          <i class="ant-spin-dot-item"></i>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    size: String
  },
  created () {
  }
}
</script>

<style lang="less">
.base-loading-layer {
  background: rgba(255,255,255,.5);
  position: absolute;
  z-index: 999999;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  .loader-outer.size-1x {
    transform: scale(0.5);
  }
}

.base-loading-layer .loader-outer{
  transform: translate(-50%, -50%);
  position: absolute;
  z-index: 999999;
  top: 50%;
  left: 50%;
  margin-top: -20px;
  margin-left: -20px;
  .ant-spin-spinning {
    position: static;
    display: inline-block;
    opacity: 1;
  }
  .ant-spin-dot {
    font-size: 28px;
  }
  .ant-spin-dot-item {
    position: absolute;
    display: block;
    width: 14px;
    height: 14px;
    background-color: #1890ff;
    border-radius: 100%;
    transform: scale(0.75);
    transform-origin: 50% 50%;
    opacity: 0.3;
    -webkit-animation: antSpinMove 1s infinite linear alternate;
            animation: antSpinMove 1s infinite linear alternate;
  }

  .ant-spin-dot-item:nth-child(1) {
    top: 0;
    left: 0;
  }
  .ant-spin-dot-item:nth-child(2) {
    top: 0;
    right: 0;
    -webkit-animation-delay: 0.4s;
            animation-delay: 0.4s;
  }
  .ant-spin-dot-item:nth-child(3) {
    right: 0;
    bottom: 0;
    -webkit-animation-delay: 0.8s;
            animation-delay: 0.8s;
  }
  .ant-spin-dot-item:nth-child(4) {
    bottom: 0;
    left: 0;
    -webkit-animation-delay: 1.2s;
            animation-delay: 1.2s;
  }
}
</style>
