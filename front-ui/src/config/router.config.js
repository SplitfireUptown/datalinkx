// eslint-disable-next-line
import { UserLayout, BasicLayout, BlankLayout } from '@/layouts'
import { bxAnaalyse } from '@/core/icons'

const RouteView = {
  name: 'RouteView',
  render: h => h('router-view')
}

export const asyncRouterMap = [
  {
    path: '/',
    name: 'index',
    component: BasicLayout,
    meta: { title: 'menu.home' },
    redirect: '/dashboard',
    children: [
      {
        path: '/datasource',
        name: 'datasource',
        redirect: '/datasource/workplace',
        component: RouteView,
        meta: { title: 'menu.datasource', keepAlive: true, icon: bxAnaalyse },
        children: [
          {
            path: '/dashboard',
            name: 'dashboard',
            component: () => import('@/views/datasource/DsList.vue'),
            meta: { title: '数据源列表', keepAlive: false }
          },
          {
            path: '/job',
            name: 'job',
            component: () => import('@/views/job/JobList.vue'),
            meta: { title: '任务列表', keepAlive: false }
          },
          {
            path: '/job_log',
            name: 'joblog',
            component: () => import('@/views/joblog/JobLogList.vue'),
            meta: { title: '任务执行日志', keepAlive: false }
          }
          // 外部链接
          // {
          //   path: 'https://www.baidu.com/',
          //   name: 'Monitor',
          //   meta: { title: 'menu.dashboard.monitor', target: '_blank' }
          // },
          // {
          //   path: '/dashboard/workplace',
          //   name: 'Workplace',
          //   component: () => import('@/views/dashboard/Workplace'),
          //   meta: { title: 'menu.dashboard.workplace', keepAlive: true, permission: ['dashboard'] }
          // }
        ]
      }
    ]
  },
  {
    path: '*',
    redirect: '/404',
    hidden: true
  }
]

/**
 * 基础路由
 * @type { *[] }
 */
export const constantRouterMap = [
  {
    path: '/user',
    component: UserLayout,
    redirect: '/user/login',
    hidden: true,
    children: [
      {
        path: 'login',
        name: 'login',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Login')
      },
      {
        path: 'register',
        name: 'register',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/Register')
      },
      {
        path: 'register-result',
        name: 'registerResult',
        component: () => import(/* webpackChunkName: "user" */ '@/views/user/RegisterResult')
      },
      {
        path: 'recover',
        name: 'recover',
        component: undefined
      }
    ]
  },

  {
    path: '/404',
    component: () => import(/* webpackChunkName: "fail" */ '@/views/exception/404')
  }
]
