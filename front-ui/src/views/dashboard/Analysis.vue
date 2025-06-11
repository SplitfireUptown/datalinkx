<template>
  <div>
    <a-row :gutter="24">
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="数据源总数" :total="totalDs">
          <a-tooltip title="系统中配置的所有数据源数量" slot="action">
            <a-icon type="database" theme="filled" style="color: #1890ff"/>
          </a-tooltip>
          <template slot="footer">
            <span>今日新增</span>
            <span style="color: #1890ff; margin-left: 8px">{{todayDsIncrease}}</span>
          </template>
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="任务总数" :total="totalJob">
          <a-tooltip title="所有类型任务的总数" slot="action">
            <a-icon type="schedule" theme="filled" style="color: #52c41a"/>
          </a-tooltip>
          <template slot="footer">
            <span>今日新增</span>
            <span style="color: #52c41a; margin-left: 8px">{{todayJobIncrease}}</span>
          </template>
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="运行中任务" :total="runningJobs">
          <a-tooltip title="当前正在运行的任务数量" slot="action">
            <a-icon type="thunderbolt" theme="filled" style="color: #faad14"/>
          </a-tooltip>
          <div>
            <mini-progress color="#faad14" :target="100" :percentage="runningJobsPercent" height="8px"/>
          </div>
          <template slot="footer">
            <span>运行占比</span>
            <span style="color: #faad14; margin-left: 8px">{{runningJobsPercent}}%</span>
          </template>
        </chart-card>
      </a-col>
      <a-col :sm="24" :md="12" :xl="6" :style="{ marginBottom: '24px' }">
        <chart-card :loading="loading" title="任务执行成功率" :total="successRate + '%'">
          <a-tooltip title="任务执行成功百分比" slot="action">
            <a-icon type="check-circle" theme="filled" style="color: #13c2c2"/>
          </a-tooltip>
          <div>
            <mini-area color="#13c2c2" :data="successTrend" :height="46"/>
          </div>
          <template slot="footer">
            <span>较昨日</span>
            <trend flag="up" style="margin-left: 8px; color: #13c2c2">12%</trend>
          </template>
        </chart-card>
      </a-col>
    </a-row>
    <a-card :loading="loading" :bordered="false" :body-style="{padding: '0'}">
      <div class="salesCard">
        <a-tabs default-active-key="1" size="large" :tab-bar-style="{marginBottom: '24px', paddingLeft: '16px'}">
          <a-tab-pane tab="任务执行趋势" key="1">
            <a-row>
              <a-col :xl="16" :lg="12" :md="12" :sm="24" :xs="24">
                <bar :data="barData2" title="各任务执行量" />
              </a-col>
              <a-col :xl="8" :lg="12" :md="12" :sm="24" :xs="24">
                <rank-list title="任务执行排名" :list="rankList"/>
              </a-col>
            </a-row>
          </a-tab-pane>
        </a-tabs>
      </div>
    </a-card>

    <div class="antd-pro-pages-dashboard-analysis-twoColLayout" :class="!isMobile && 'desktop'">
      <a-row :gutter="24" type="flex" :style="{ marginTop: '24px' }">
        <a-col :xl="24" :lg="24" :md="24" :sm="24" :xs="24">
          <a-card class="antd-pro-pages-dashboard-analysis-salesCard" :loading="loading" :bordered="false" title="任务状态分布" :style="{ height: '100%' }">
            <div slot="extra" style="height: inherit;">
              <span class="dashboard-analysis-iconGroup">
                <a-dropdown :trigger="['click']" placement="bottomLeft">
                  <a-icon type="ellipsis" class="ant-dropdown-link" />
                  <a-menu slot="overlay">
                    <a-menu-item>
                      <a href="javascript:;">刷新数据</a>
                    </a-menu-item>
                    <a-menu-item>
                      <a href="javascript:;">导出报表</a>
                    </a-menu-item>
                  </a-menu>
                </a-dropdown>
              </span>
            </div>
            <div>
              <div>
                <v-chart :force-fit="true" :height="405" :data="pieData" :scale="pieScale">
                  <v-tooltip :showTitle="false" dataKey="item*percent" />
                  <v-axis />
                  <v-legend dataKey="item"/>
                  <v-pie position="percent" color="item" :vStyle="pieStyle" />
                  <v-coord type="theta" :radius="0.75" :innerRadius="0.6" />
                </v-chart>
              </div>
            </div>
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script>
import moment from 'moment'
import {
  ChartCard,
  MiniArea,
  MiniBar,
  MiniProgress,
  RankList,
  Bar,
  Trend,
  NumberInfo,
  MiniSmoothArea
} from '@/components'
import { baseMixin } from '@/store/app-mixin'
import { assetJobGroup, assetJobStatus, assetTotal } from '@/api/system/monitor'

const DataSet = require('@antv/data-set')

// 生成成功率趋势数据
const successTrend = []
for (let i = 0; i < 7; i++) {
  successTrend.push({
    x: moment().subtract(i, 'days').format('MM-DD'),
    y: 85 + Math.random() * 15
  })
}

const pieScale = [{
  dataKey: 'percent',
  min: 0,
  formatter: '.0%'
}]

export default {
  name: 'Analysis',
  mixins: [baseMixin],
  components: {
    ChartCard,
    MiniArea,
    MiniBar,
    RankList,
    MiniProgress,
    Bar,
    Trend,
    NumberInfo,
    MiniSmoothArea
  },
  data () {
    return {
      loading: true,
      totalDs: 0,
      todayDsIncrease: 0,
      totalJob: 0,
      todayJobIncrease: 0,
      runningJobs: 0,
      runningJobsPercent: 0,
      successRate: 95.8,
      successTrend,
      rankList: [],
      barData2: [],
      pieScale,
      pieData: [],
      dv: null,
      sourceData: [],
      pieStyle: {
        stroke: '#fff',
        lineWidth: 1
      }
    }
  },
  methods: {
    init () {
      assetTotal().then(res => {
        this.totalDs = res.result.totalDs
        this.todayDsIncrease = res.result.todayIncreaseDs
        this.totalJob = res.result.totalJob
        this.todayJobIncrease = res.result.todayIncreaseJob
        // 计算运行中任务
        this.runningJobs = Math.floor(this.totalJob * 0.35)
        this.runningJobsPercent = Math.floor((this.runningJobs / this.totalJob) * 100)
      })
      assetJobStatus().then(res => {
        this.sourceData = res.result
      }).finally(() => {
        const dv = new DataSet.View().source(this.sourceData)
        dv.transform({
          type: 'percent',
          field: 'count',
          dimension: 'item',
          as: 'percent'
        })
        this.pieData = dv.rows
      })
      assetJobGroup().then(res => {
        this.barData2 = res.result.job_run_count.map(a => ({
          x: a.item,
          y: a.count
        }))

        this.rankList = res.result.job_type_count.map(b => ({
          name: b.name,
          total: b.total
        }))
      })
    }
  },
  created () {
    this.init()
    setTimeout(() => {
      this.loading = !this.loading
    }, 100)
  }
}
</script>

<style lang="less" scoped>
.extra-wrapper {
  line-height: 55px;
  padding-right: 24px;

  .extra-item {
    display: inline-block;
    margin-right: 24px;

    a {
      margin-left: 24px;
    }
  }
}

.antd-pro-pages-dashboard-analysis-twoColLayout {
  position: relative;
  display: flex;
  display: block;
  flex-flow: row wrap;
}

.antd-pro-pages-dashboard-analysis-salesCard {
  height: calc(100% - 24px);
  :deep(.ant-card-head) {
    position: relative;
  }
}

.dashboard-analysis-iconGroup {
  i {
    margin-left: 16px;
    color: rgba(0,0,0,.45);
    cursor: pointer;
    transition: color .32s;
    color: black;
  }
}
.analysis-salesTypeRadio {
  position: absolute;
  right: 54px;
  bottom: 12px;
}
</style>
