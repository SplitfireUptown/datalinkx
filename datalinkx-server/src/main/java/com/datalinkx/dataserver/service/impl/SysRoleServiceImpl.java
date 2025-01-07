package com.datalinkx.dataserver.service.impl;

import com.datalinkx.dataserver.bean.domain.SysRoleBean;
import com.datalinkx.dataserver.bean.domain.SysUserRoleBean;
import com.datalinkx.dataserver.repository.SysRoleRepository;
import com.datalinkx.dataserver.service.ISysRoleService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 角色 业务层处理
 *
 * @author ruoyi
 */
@Service
public class SysRoleServiceImpl implements ISysRoleService {
    @Autowired
    SysRoleRepository sysRoleRepository;

    @Override
    public List<SysRoleBean> selectRoleList(SysRoleBean role) {
        return null;
    }

    @Override
    public List<SysRoleBean> selectRolesByUserId(Long userId) {
        return null;
    }

    @Override
    public Set<String> selectRolePermissionByUserId(Long userId) {
        List<SysRoleBean> perms = sysRoleRepository.selectRolePermissionByUserId(userId);
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
        return null;
    }

    @Override
    public List<Long> selectRoleListByUserId(Long userId) {
        return null;
    }

    @Override
    public SysRoleBean selectRoleById(Long roleId) {
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
    public int countUserRoleByRoleId(Long roleId) {
        return 0;
    }

    @Override
    public int insertRole(SysRoleBean role) {
        return 0;
    }

    @Override
    public int updateRole(SysRoleBean role) {
        return 0;
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
    public int deleteRoleById(Long roleId) {
        return 0;
    }

    @Override
    public int deleteRoleByIds(Long[] roleIds) {
        return 0;
    }

    @Override
    public int deleteAuthUser(SysUserRoleBean userRole) {
        return 0;
    }

    @Override
    public int deleteAuthUsers(Long roleId, Long[] userIds) {
        return 0;
    }

    @Override
    public int insertAuthUsers(Long roleId, Long[] userIds) {
        return 0;
    }
}
