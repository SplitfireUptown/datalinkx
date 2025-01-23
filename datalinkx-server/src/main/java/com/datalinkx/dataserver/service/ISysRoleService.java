package com.datalinkx.dataserver.service;


import com.datalinkx.dataserver.bean.domain.SysRoleBean;
import com.datalinkx.dataserver.bean.domain.SysUserBean;
import com.datalinkx.dataserver.bean.domain.SysUserRoleBean;

import java.util.List;
import java.util.Set;

/**
 * 角色业务层
 *
 * @author ruoyi
 */
public interface ISysRoleService {
    /**
     * 根据条件分页查询角色数据
     *
     * @param role 角色信息
     * @return 角色数据集合信息
     */
    public List<SysRoleBean> selectRoleList(SysRoleBean role);

    /**
     * 根据用户ID查询角色列表
     *
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<SysRoleBean> selectRolesByUserId(String userId);

    /**
     * 根据用户ID查询角色权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    public Set<String> selectRolePermissionByUserId(String userId);

    /**
     * 查询所有角色
     *
     * @return 角色列表
     */
    public List<SysRoleBean> selectRoleAll();

    /**
     * 根据用户ID获取角色选择框列表
     *
     * @param userId 用户ID
     * @return 选中角色ID列表
     */
    public List<Long> selectRoleListByUserId(String userId);

    /**
     * 通过角色ID查询角色
     *
     * @param roleId 角色ID
     * @return 角色对象信息
     */
    public SysRoleBean selectRoleById(String roleId);

    /**
     * 校验角色名称是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    public boolean checkRoleNameUnique(SysRoleBean role);

    /**
     * 校验角色权限是否唯一
     *
     * @param role 角色信息
     * @return 结果
     */
    public boolean checkRoleKeyUnique(SysRoleBean role);

    /**
     * 校验角色是否允许操作
     *
     * @param role 角色信息
     */
    public void checkRoleAllowed(SysRoleBean role);

    /**
     * 校验角色是否有数据权限
     *
     * @param roleIds 角色id
     */
    public void checkRoleDataScope(Long... roleIds);

    /**
     * 通过角色ID查询角色使用数量
     *
     * @param roleId 角色ID
     * @return 结果
     */
    public int countUserRoleByRoleId(String roleId);

    /**
     * 新增保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    public int insertRole(SysRoleBean role);

    /**
     * 修改保存角色信息
     *
     * @param role 角色信息
     * @return 结果
     */
    public int updateRole(SysRoleBean role);

    /**
     * 修改角色状态
     *
     * @param role 角色信息
     * @return 结果
     */
    public int updateRoleStatus(SysRoleBean role);

    /**
     * 修改数据权限信息
     *
     * @param role 角色信息
     * @return 结果
     */
    public int authDataScope(SysRoleBean role);

    /**
     * 通过角色ID删除角色
     *
     * @param roleId 角色ID
     * @return 结果
     */
    public int deleteRoleById(String roleId);

    /**
     * 批量删除角色信息
     *
     * @param roleIds 需要删除的角色ID
     * @return 结果
     */
    public int deleteRoleByIds(String[] roleIds);

    /**
     * 取消授权用户角色
     *
     * @param userRole 用户和角色关联信息
     * @return 结果
     */
    public int deleteAuthUser(SysUserRoleBean userRole);

    /**
     * 批量取消授权用户角色
     *
     * @param roleId  角色ID
     * @return 结果
     */
    public int deleteAuthUsers(String roleId);

    /**
     * 批量取消授权角色菜单
     * @param roleId 角色ID
     */
    public int deleteAuthMenus(String roleId);

    /**
     * 批量选择授权角色菜单
     * @param roleId 角色ID
     * @param menuIds 需要选择授权的菜单数据ID
     */
    public int insertAuthMenus(String roleId, String[] menuIds);

    /**
     * 批量选择授权用户角色
     *
     * @param roleId  角色ID
     * @param userIds 需要删除的用户数据ID
     * @return 结果
     */
    public int insertAuthUsers(String roleId, String[] userIds);

    /**
     * 查询角色已分配用户列表
     *
     * @param roleId 用户信息
     * @return 用户信息集合
     */
    public List<SysUserBean> selectUserListByRoleId(String roleId);
}
