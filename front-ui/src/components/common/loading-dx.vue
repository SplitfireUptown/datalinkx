<template>
  <div class="base-loading-layer">
    <div :class="['size']" class="loader-outer">
      <div class="loader-inner line-scale-pulse-out-rapid">
        <div></div><div></div><div></div><div></div><div></div>
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
        position: absolute;
        z-index: 999999;
        top: 0;
        right: 0;
        bottom: 0;
        left: 0;
      }
      .base-loading-layer .loader-outer{
        position: absolute;
        z-index: 999999;
        top: 50%;
        left: 50%;
        width: 40px;
        height: 40px;
        margin-top: -20px;
        margin-left: -20px;
      }
      .loader-outer.size-1x {
        transform: scale(0.5);
      }
      .line-scale-pulse-out-rapid>div {
        background-color: #5484E1;
        width: 4px;
        height: 35px;
        border-radius: 2px;
        margin: 2px;
        display: inline-block;
        -webkit-animation: line-scale-pulse-out-rapid .9s 0s infinite cubic-bezier(.11, .49, .38, .78);
        animation: line-scale-pulse-out-rapid .9s 0s infinite cubic-bezier(.11, .49, .38, .78)
      }
      .line-scale-pulse-out-rapid>div:nth-child(2), .line-scale-pulse-out-rapid>div:nth-child(4) {
        -webkit-animation-delay: .25s !important;
        animation-delay: .25s !important
      }
      .line-scale-pulse-out-rapid>div:nth-child(1), .line-scale-pulse-out-rapid>div:nth-child(5) {
        -webkit-animation-delay: .5s !important;
        animation-delay: .5s !important
      }

      @-webkit-keyframes line-scale-pulse-out-rapid {
        0%, 90% {
          -webkit-transform: scaley(1);
          transform: scaley(1)
        }
        80% {
          -webkit-transform: scaley(.3);
          transform: scaley(.3)
        }
      }
      @keyframes line-scale-pulse-out-rapid {
        0%, 90% {
          -webkit-transform: scaley(1);
          transform: scaley(1)
        }
        80% {
          -webkit-transform: scaley(.3);
          transform: scaley(.3)
        }
      }
</style>
