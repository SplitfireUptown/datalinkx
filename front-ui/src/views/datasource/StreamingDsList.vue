<template>
  <div class="ds-list-root">
    <a-card :bordered="false" class="list-acard">
      <div class="table-page-search-wrapper">
        <div style="text-align: center;">敬请期待……</div>
      </div>
    </a-card>
  </div>
</template>

<script>

export default {
  name: 'StreamingDsList',
  components: {
  },
  data () {
    return {
    }
  },
  methods: {
  },
  created () {
  }
}
</script>

<style scoped lang="less">
.ds-list-root {
  position: relative;
  display: flex;
  height: 100%;
  width: 100%;
  .list-left {
    width: 180px;
    height: 100%;
  }
  .list-acard {
    flex: 1;
    margin: 24px 0 16px 24px;
  }
  .list-left {
    border-radius: 0px;
    padding: 0 8px;
    overflow: auto;
    cursor: pointer;
    background-color: #fff;
    box-shadow: 0px 0px 1px rgba(15, 34, 67, 0.3), 0px 1px 3px rgba(15, 34, 67, 0.08), 0px 4px 8px rgba(15, 34, 67, 0.03);
    ul, li {
      list-style: none;
      padding: 0;
      margin: 0;
    }
    ul {
      margin-top: 4px;
    }
    .ds-list {
      line-height: 40px;
      padding-left: 16px;
      height: 40px;
      position: relative;
      border-radius: 6px;
      cursor: pointer;
      &:hover {
        background-color: rgba(15, 34, 67, 0.05);
      }
      .ds-icon {
        float: left;
        width: 24px;
        height: 24px;
        img {
          width: 24px;
          height: 24px;
          margin: 0;
          padding: 0;
          border: 0;
        }
      }
      .ds-name {
        margin-left: 8px;
        line-height: 20px;
        padding-top: 10px;
        padding-bottom: 10px;
        display: inline-block;
        max-width: 130px;
        .nowrap {
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
        .name {
          line-height: 20px;
          height: 20px;
          font-size: 14px;
          display: inline-block;
          color: rgba(21, 22, 24, 0.72);
          max-width: 90px;
          text-overflow: ellipsis;
          overflow: hidden;
          white-space: nowrap;
        }
        .num-in {
          top: 0;
          line-height: 14px;
          padding: 3px 4px;
          background-color: rgba(15, 34, 67, 0.07);
          color: rgba(15, 34, 67, 0.48);
          border-radius: 4px;
          font-size: 10px;
          display: inline-block;
          vertical-align: super;
          transform: scale(0.86);
        }
      }
    }
    .active {
      background-color: rgba(43, 121, 255, 0.1);
    }
  }

}
</style>
