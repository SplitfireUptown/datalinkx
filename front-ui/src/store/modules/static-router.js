import { asyncRouterMap, constantRouterMap } from '@/config/router.config'
import cloneDeep from 'lodash.clonedeep'
import { getCurrentUserNav } from '@/api/login'
import { notFoundRouter, transformMenuToRoutes } from '@/router/generator-routers'

/**
 * 过滤账户是否拥有某一个权限，并将菜单从加载列表移除
 *
 * @param permission
 * @param route
 * @returns {boolean}
 */
function hasPermission (permission, route) {
  if (route.meta && route.meta.permission) {
    console.log('hasPermission', permission)
    if (permission === undefined) {
      return false
    }
    let flag = false
    for (let i = 0, len = permission.length; i < len; i++) {
      flag = route.meta.permission.includes(permission[i])
      if (flag) {
        return true
      }
    }
    return false
  }
  return true
}

/**
 * 单账户多角色时，使用该方法可过滤角色不存在的菜单
 *
 * @param roles
 * @param route
 * @returns {*}
 */
// eslint-disable-next-line
function hasRole(roles, route) {
  if (route.meta && route.meta.roles) {
    return route.meta.roles.includes(roles.id)
  } else {
    return true
  }
}

function filterAsyncRouter (routerMap, permissions) {
  const accessedRouters = routerMap.filter(route => {
    if (hasPermission(permissions, route)) {
      if (route.children && route.children.length) {
        route.children = filterAsyncRouter(route.children, permissions)
      }
      return true
    }
    return false
  })
  return accessedRouters
}

const permission = {
  state: {
    routers: constantRouterMap,
    addRouters: []
  },
  mutations: {
    SET_ROUTERS: (state, routers) => {
      state.addRouters = routers
      state.routers = constantRouterMap.concat(routers)
    }
  },
  actions: {
    GenerateRoutes ({ commit }, data) {
      return new Promise(resolve => {
        const { permissions } = data
        const routerMap = cloneDeep(asyncRouterMap)
        const accessedRouters = filterAsyncRouter(routerMap, permissions)
        getCurrentUserNav().then(res => {
          const syncRouters = transformMenuToRoutes(res.result)
          // 合并后端返回的菜单和前端配置的菜单
          const routers = syncRouters.concat(accessedRouters)
          // 去重
          const newRouters = []
          routers.forEach(item => {
            const index = newRouters.findIndex(r => r.path === item.path)
            if (index === -1) {
              newRouters.push(item)
            }
          })
          newRouters.push(notFoundRouter)
          commit('SET_ROUTERS', newRouters)
          resolve()
        }).catch(() => {
          console.log('获取用户菜单失败')
        })
      })
    }
  }
}

export default permission
