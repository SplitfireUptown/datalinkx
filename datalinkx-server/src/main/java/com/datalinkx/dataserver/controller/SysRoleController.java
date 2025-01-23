package com.datalinkx.dataserver.controller;


import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.domain.SysMenuBean;
import com.datalinkx.dataserver.bean.domain.SysRoleBean;
import com.datalinkx.dataserver.bean.domain.SysUserBean;
import com.datalinkx.dataserver.bean.model.AuthMenuBody;
import com.datalinkx.dataserver.bean.model.AuthUserBody;
import com.datalinkx.dataserver.repository.SysRoleRepository;
import com.datalinkx.dataserver.service.ISysMenuService;
import com.datalinkx.dataserver.service.ISysRoleService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;

import static com.datalinkx.common.utils.ObjectUtils.getNullPropertyNames;

/**
 * 角色信息
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController {
    @Autowired
    private ISysRoleService roleService;
    @Autowired
    private SysRoleRepository sysRoleRepository;
    @Autowired
    private ISysMenuService menuService;

    /**
     * 获取角色列表
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/list")
    public WebResult<List<SysRoleBean>> selectRoleList() {

        return WebResult.of(roleService.selectRoleAll());
    }

    /**
     * 更新角色
     *
     * @param role
     */
    @PutMapping("/update")
    public WebResult<HashMap<String, Integer>> updateRole(@RequestBody SysRoleBean role) {
        int count = 0;
        if (role.getRoleId() != null) {
            SysRoleBean sysRoleBean = sysRoleRepository.findById(role.getRoleId()).orElse(new SysRoleBean());
            BeanUtils.copyProperties(role, sysRoleBean, getNullPropertyNames(role));
            count = roleService.updateRole(sysRoleBean);
        } else {
            count = roleService.insertRole(role);
        }
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count", count);
        return WebResult.of(resultMap);
    }

    /**
     * 新增角色
     *
     * @param role
     */
    @PostMapping("/insert")
    public WebResult<HashMap<String, Integer>> insertMenu(@RequestBody SysRoleBean role) {
        int count = roleService.insertRole(role);
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count", count);
        return WebResult.of(resultMap);
    }

    /**
     * 删除角色
     *
     * @param roleId
     */
    @DeleteMapping("/delete")
    public WebResult<HashMap<String, Integer>> deleteRole(@RequestBody String[] roleIds) {
        int count = roleService.deleteRoleByIds(roleIds);
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count", count);
        return WebResult.of(resultMap);
    }

    /**
     * 批量授权角色用户
     */
    @PostMapping("/authUserList")
    @Transactional(rollbackOn = Exception.class)
    public WebResult<HashMap<String, Integer>> authUser(@RequestBody AuthUserBody authUserBody) {
        int deleted = roleService.deleteAuthUsers(authUserBody.getRoleId());
        int count = roleService.insertAuthUsers(authUserBody.getRoleId(), authUserBody.getUserIds());
        if (count != authUserBody.getUserIds().length) {
            throw new DatalinkXServerException("授权用户失败");
        }
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count", count);
        resultMap.put("deleted", deleted);
        return WebResult.of(resultMap);
    }

    /**
     * 查询角色已授权用户列表
     *
     * @param roleId
     */
    @GetMapping("/authUserList")
    public WebResult<HashMap<String, List<SysUserBean>>> authUser(@RequestParam String roleId) {
        List<SysUserBean> sysUserBeans = roleService.selectUserListByRoleId(roleId);
        HashMap<String, List<SysUserBean>> resultMap = new HashMap<>();
        resultMap.put("userList", sysUserBeans);
        return WebResult.of(resultMap);
    }

    /**
     * 查询角色菜单列表
     *
     *
     * @param roleId
     */
    @GetMapping("/authMenuList")
    public WebResult<HashMap<String, List<SysMenuBean>>> authMenu(@RequestParam String roleId) {
        List<SysMenuBean> sysMenuBeans = menuService.selectMenuListByRoleId(roleId);
        HashMap<String, List<SysMenuBean>> resultMap = new HashMap<>();
        resultMap.put("menuList", sysMenuBeans);
        return WebResult.of(resultMap);
    }

    /**
     * 批量授权角色菜单
     * @param roleId
     * @param menuIds
     */
    @PostMapping("/authMenuList")
    @Transactional(rollbackOn = Exception.class)
    public WebResult<HashMap<String, Integer>> authMenu(@RequestBody AuthMenuBody authMenuBody) {
        int deleted = roleService.deleteAuthMenus(authMenuBody.getRoleId());
        int count = roleService.insertAuthMenus(authMenuBody.getRoleId(), authMenuBody.getMenuIds());
        if (count != authMenuBody.getMenuIds().length) {
            throw new DatalinkXServerException("授权菜单失败");
        }
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count", count);
        resultMap.put("deleted", deleted);
        return WebResult.of(resultMap);
    }
}
