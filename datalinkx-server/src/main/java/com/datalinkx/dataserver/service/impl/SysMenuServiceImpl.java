package com.datalinkx.dataserver.service.impl;

import com.datalinkx.common.constants.MetaConstants;
import com.datalinkx.common.constants.UserConstants;
import com.datalinkx.dataserver.bean.domain.SysMenuBean;
import com.datalinkx.dataserver.bean.domain.TreeSelect;
import com.datalinkx.dataserver.bean.vo.MetaVo;
import com.datalinkx.dataserver.bean.vo.RouterVo;
import com.datalinkx.dataserver.repository.SysMenuRepository;
import com.datalinkx.dataserver.service.ISysMenuService;
import com.datalinkx.dataserver.utils.SecurityUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SysMenuServiceImpl implements ISysMenuService {
    @Autowired
    private SysMenuRepository sysMenuRepository;

    @Override
    public List<SysMenuBean> selectMenuList(String userId) {
        return sysMenuRepository.selectMenuTreeByUserId(userId);
    }

    @Override
    public List<SysMenuBean> selectMenuList(SysMenuBean menu, String userId) {
        return null;
    }

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByUserId(String userId) {
        List<String> perms = sysMenuRepository.selectMenuPermsByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    /**
     * 根据角色ID查询权限
     *
     * @param roleId 角色ID
     * @return 权限列表
     */
    @Override
    public Set<String> selectMenuPermsByRoleId(String roleId) {
        List<String> perms = sysMenuRepository.selectMenuPermsByRoleId(roleId);
        Set<String> permsSet = new HashSet<>();
        for (String perm : perms) {
            if (StringUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.trim().split(",")));
            }
        }
        return permsSet;
    }

    @Override
    public List<SysMenuBean> selectMenuTreeByUserId(String userId) {
        List<SysMenuBean> menus = null;
        if (SecurityUtils.isAdmin(userId)) {
            menus = sysMenuRepository.selectMenuTreeAll();
        } else {
            menus = sysMenuRepository.selectMenuTreeByUserId(userId);
        }
        return getChildPerms(menus, "0");
    }

    @Override
    public List<Long> selectMenuListByRoleId(String roleId) {
        return null;
    }

    /**
     * 构建前端路由所需要的菜单
     *
     * @param menus 菜单列表
     * @return 路由列表
     */
    @Override
    public List<RouterVo> buildMenus(List<SysMenuBean> menus) {
        List<RouterVo> routers = new LinkedList<RouterVo>();
        for (SysMenuBean menu : menus) {
            RouterVo router = new RouterVo();
            router.setHidden("1".equals(menu.getVisible()));
            router.setName(getRouteName(menu));
            router.setPath(getRouterPath(menu));
            router.setComponent(getComponent(menu));
            router.setQuery(menu.getQuery());
            router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("0", menu.getIsCache().toString()), menu.getPath()));
            List<SysMenuBean> cMenus = menu.getChildren();
            if (ObjectUtils.isNotEmpty(cMenus) && UserConstants.TYPE_DIR.equals(menu.getMenuType())) {
                router.setAlwaysShow(true);
                router.setRedirect("noRedirect");
                router.setChildren(buildMenus(cMenus));
            } else if (isMenuFrame(menu)) {
                router.setMeta(null);
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                children.setPath(menu.getPath());
                children.setComponent(menu.getComponent());
                children.setName(getRouteName(menu.getRouteName(), menu.getPath()));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), StringUtils.equals("1", menu.getIsCache().toString()), menu.getPath()));
                children.setQuery(menu.getQuery());
                childrenList.add(children);
                router.setChildren(childrenList);
            } else if (menu.getParentId().equals("0") && isInnerLink(menu)) {
                router.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon()));
                router.setPath("/");
                List<RouterVo> childrenList = new ArrayList<RouterVo>();
                RouterVo children = new RouterVo();
                String routerPath = innerLinkReplaceEach(menu.getPath());
                children.setPath(routerPath);
                children.setComponent(UserConstants.INNER_LINK);
                children.setName(getRouteName(menu.getRouteName(), routerPath));
                children.setMeta(new MetaVo(menu.getMenuName(), menu.getIcon(), menu.getPath()));
                childrenList.add(children);
                router.setChildren(childrenList);
            }
            routers.add(router);
        }
        return routers;
    }

    @Override
    public List<SysMenuBean> buildMenuTree(List<SysMenuBean> menus) {
        return null;
    }

    @Override
    public List<TreeSelect> buildMenuTreeSelect(List<SysMenuBean> menus) {
        return null;
    }

    @Override
    public SysMenuBean selectMenuById(String menuId) {
        return null;
    }

    @Override
    public boolean hasChildByMenuId(String menuId) {
        return false;
    }

    @Override
    public boolean checkMenuExistRole(String menuId) {
        return false;
    }

    @Override
    public int insertMenu(SysMenuBean menu) {
        return 0;
    }

    @Override
    public int updateMenu(SysMenuBean menu) {
        return 0;
    }

    @Override
    public int deleteMenuById(String menuId) {
        return 0;
    }

    @Override
    public boolean checkMenuNameUnique(SysMenuBean menu) {
        return false;
    }

    /**
     * 根据父节点的ID获取所有子节点
     *
     * @param list     分类表
     * @param parentId 传入的父节点ID
     * @return String
     */
    public List<SysMenuBean> getChildPerms(List<SysMenuBean> list, String parentId) {
        List<SysMenuBean> returnList = new ArrayList<SysMenuBean>();
        for (Iterator<SysMenuBean> iterator = list.iterator(); iterator.hasNext(); ) {
            SysMenuBean t = (SysMenuBean) iterator.next();
            // 一、根据传入的某个父节点ID,遍历该父节点的所有子节点
            if (Objects.equals(t.getParentId(), parentId)) {
                recursionFn(list, t);
                returnList.add(t);
            }
        }
        return returnList;
    }

    /**
     * 递归列表
     *
     * @param list 分类表
     * @param t    子节点
     */
    private void recursionFn(List<SysMenuBean> list, SysMenuBean t) {
        // 得到子节点列表
        List<SysMenuBean> childList = getChildList(list, t);
        t.setChildren(childList);
        for (SysMenuBean tChild : childList) {
            if (hasChild(list, tChild)) {
                recursionFn(list, tChild);
            }
        }
    }

    /**
     * 得到子节点列表
     */
    private List<SysMenuBean> getChildList(List<SysMenuBean> list, SysMenuBean t) {
        List<SysMenuBean> tlist = new ArrayList<SysMenuBean>();
        Iterator<SysMenuBean> it = list.iterator();
        while (it.hasNext()) {
            SysMenuBean n = (SysMenuBean) it.next();
            if (Objects.equals(n.getParentId(), t.getMenuId())) {
                tlist.add(n);
            }
        }
        return tlist;
    }

    /**
     * 判断是否有子节点
     */
    private boolean hasChild(List<SysMenuBean> list, SysMenuBean t) {
        return getChildList(list, t).size() > 0;
    }

    /**
     * 获取路由名称
     *
     * @param menu 菜单信息
     * @return 路由名称
     */
    public String getRouteName(SysMenuBean menu) {
        // 非外链并且是一级目录（类型为目录）
        if (isMenuFrame(menu)) {
            return StringUtils.EMPTY;
        }
        return getRouteName(menu.getRouteName(), menu.getPath());
    }

    /**
     * 获取路由地址
     *
     * @param menu 菜单信息
     * @return 路由地址
     */
    public String getRouterPath(SysMenuBean menu) {
        String routerPath = menu.getPath();
        // 内链打开外网方式
        if (!menu.getParentId().equals("0") && isInnerLink(menu)) {
            routerPath = innerLinkReplaceEach(routerPath);
        }
        // 非外链并且是一级目录（类型为目录）
        if (menu.getParentId().equals("0") && UserConstants.TYPE_DIR.equals(menu.getMenuType())
                && UserConstants.NO_FRAME.equals(menu.getIsFrame()) && !routerPath.equals("/")) {
            routerPath = "/" + menu.getPath();
        }
        // 非外链并且是一级目录（类型为菜单）
        else if (isMenuFrame(menu) && !routerPath.equals("/")) {
            routerPath = "/";
        }
        return routerPath;
    }

    /**
     * 获取组件信息
     *
     * @param menu 菜单信息
     * @return 组件信息
     */
    public String getComponent(SysMenuBean menu) {
        String component = UserConstants.LAYOUT;
        if (StringUtils.isNotEmpty(menu.getComponent()) && !isMenuFrame(menu)) {
            component = menu.getComponent();
        } else if (StringUtils.isEmpty(menu.getComponent()) && !menu.getParentId().equals("0") && isInnerLink(menu)) {
            component = UserConstants.INNER_LINK;
        } else if (StringUtils.isEmpty(menu.getComponent()) && isParentView(menu)) {
            component = UserConstants.PARENT_VIEW;
        }
        return component;
    }


    /**
     * 是否为菜单内部跳转
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isMenuFrame(SysMenuBean menu) {
        return menu.getParentId().equals("0") && UserConstants.TYPE_MENU.equals(menu.getMenuType())
                && menu.getIsFrame().equals(UserConstants.NO_FRAME);
    }

    /**
     * 获取路由名称，如没有配置路由名称则取路由地址
     *
     * @param name 路由名称
     * @param path 路由地址
     * @return 路由名称（驼峰格式）
     */
    public String getRouteName(String name, String path) {
        String routerName = StringUtils.isNotEmpty(name) ? name : path;
        return StringUtils.capitalize(routerName);
    }

    /**
     * 是否为内链组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isInnerLink(SysMenuBean menu) {
        return menu.getIsFrame().equals(UserConstants.NO_FRAME) && StringUtils.startsWithAny(menu.getPath(), MetaConstants.CommonConstant.HTTP, MetaConstants.CommonConstant.HTTPS);
    }

    /**
     * 是否为parent_view组件
     *
     * @param menu 菜单信息
     * @return 结果
     */
    public boolean isParentView(SysMenuBean menu) {
        return !menu.getParentId().equals("0") && UserConstants.TYPE_DIR.equals(menu.getMenuType());
    }

    /**
     * 内链域名特殊字符替换
     *
     * @return 替换后的内链域名
     */
    public String innerLinkReplaceEach(String path) {
        return StringUtils.replaceEach(path, new String[]{MetaConstants.CommonConstant.HTTP, MetaConstants.CommonConstant.HTTPS, MetaConstants.CommonConstant.WWW, ".", ":"},
                new String[]{"", "", "", "/", "/"});
    }
}
