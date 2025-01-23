package com.datalinkx.dataserver.service.impl;

import com.datalinkx.dataserver.bean.domain.SysRoleBean;
import com.datalinkx.dataserver.bean.domain.SysRoleMenuBean;
import com.datalinkx.dataserver.bean.domain.SysUserBean;
import com.datalinkx.dataserver.bean.domain.SysUserRoleBean;
import com.datalinkx.dataserver.repository.*;
import com.datalinkx.dataserver.service.ISysRoleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 角色 业务层处理
 *
 * @author ruoyi
 */
@Slf4j
@Service
public class SysRoleServiceImpl implements ISysRoleService {
    @Autowired
    SysRoleRepository sysRoleRepository;
    @Autowired
    SysUserRepository SysUserRepository;
    @Autowired
    SysUserRoleRepository sysUserRoleRepository;
    @Autowired
    private SysMenuRepository sysMenuRepository;
    @Autowired
    private SysRoleMenuRepository sysRoleMenuRepository;

    @Override
    public List<SysRoleBean> selectRoleList(SysRoleBean role) {
        return null;
    }

    @Override
    public List<SysRoleBean> selectRolesByUserId(String userId) {
        return null;
    }

    @Override
    public Set<String> selectRolePermissionByUserId(String userId) {
        List<SysRoleBean> perms = sysRoleRepository.selectRoleByUserId(userId);
        Set<String> permsSet = new HashSet<>();
        for (SysRoleBean perm : perms) {
            if (ObjectUtils.isNotEmpty(perm)) {
                permsSet.addAll(Arrays.asList(perm.getRoleKey().trim().split(",")));
            }
        }
        return permsSet;
    }

    @Override
    public List<SysRoleBean> selectRoleAll() {
        return sysRoleRepository.selectRoleAll();
    }

    @Override
    public List<Long> selectRoleListByUserId(String userId) {
        return null;
    }

    @Override
    public SysRoleBean selectRoleById(String roleId) {
        return null;
    }

    @Override
    public boolean checkRoleNameUnique(SysRoleBean role) {
        return false;
    }

    @Override
    public boolean checkRoleKeyUnique(SysRoleBean role) {
        return false;
    }

    @Override
    public void checkRoleAllowed(SysRoleBean role) {

    }

    @Override
    public void checkRoleDataScope(Long... roleIds) {

    }

    @Override
    public int countUserRoleByRoleId(String roleId) {
        return 0;
    }

    @Override
    public int insertRole(SysRoleBean role) {
        return sysRoleRepository.save(role) != null ? 1 : 0;
    }

    @Override
    public int updateRole(SysRoleBean role) {
        return sysRoleRepository.save(role) != null ? 1 : 0;
    }

    @Override
    public int updateRoleStatus(SysRoleBean role) {
        return 0;
    }

    @Override
    public int authDataScope(SysRoleBean role) {
        return 0;
    }

    @Override
    public int deleteRoleById(String roleId) {
        sysRoleRepository.deleteById(roleId);
        return 1;
    }

    @Override
    public int deleteRoleByIds(String[] roleIds) {
        return sysRoleRepository.deleteByIds(roleIds);
    }

    @Override
    public int deleteAuthUser(SysUserRoleBean userRole) {
        return 0;
    }

    @Override
    public int deleteAuthUsers(String roleId) {
        return sysUserRoleRepository.deleteAuthUsers(roleId);
    }

    @Override
    public int deleteAuthMenus(String roleId) {
        return sysRoleMenuRepository.deleteAuthMenus(roleId);
    }

    @Override
    public int insertAuthMenus(String roleId, String[] menuIds) {
        List<SysRoleMenuBean> roleMenus = Arrays.stream(menuIds)
                .map(menuId -> new SysRoleMenuBean(roleId, menuId))
                .collect(Collectors.toList());
        return sysRoleMenuRepository.saveAll(roleMenus).size();
    }

    @Override
    public int insertAuthUsers(String roleId, String[] userIds) {
        List<SysUserRoleBean> userRoles = Arrays.stream(userIds)
                .map(userId -> new SysUserRoleBean(userId, roleId))
                .collect(Collectors.toList());
        return sysUserRoleRepository.saveAll(userRoles).size();
    }

    @Override
    public List<SysUserBean> selectUserListByRoleId(String roleId) {
        return SysUserRepository.selectUserListByRoleId(roleId);
    }
}
