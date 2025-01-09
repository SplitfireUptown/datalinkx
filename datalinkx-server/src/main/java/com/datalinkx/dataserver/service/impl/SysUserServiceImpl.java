package com.datalinkx.dataserver.service.impl;

import com.datalinkx.dataserver.bean.domain.SysUserBean;
import com.datalinkx.dataserver.repository.SysUserRepository;
import com.datalinkx.dataserver.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserServiceImpl implements ISysUserService {
    @Autowired
    private SysUserRepository sysUserRepository;

    @Override
    public List<SysUserBean> selectUserList(SysUserBean user) {
        return null;
    }

    @Override
    public List<SysUserBean> selectAllocatedList(SysUserBean user) {
        return null;
    }

    @Override
    public List<SysUserBean> selectUnallocatedList(SysUserBean user) {
        return null;
    }

    @Override
    public SysUserBean selectUserByUserName(String userName) {
        return sysUserRepository.selectUserByUserName(userName);
    }

    @Override
    public SysUserBean selectUserById(String userId) {
        return sysUserRepository.selectUserById(userId);
    }

    @Override
    public SysUserBean selectUserById(Long userId) {
        return null;
    }

    @Override
    public String selectUserRoleGroup(String userName) {
        return null;
    }

    @Override
    public String selectUserPostGroup(String userName) {
        return null;
    }

    @Override
    public boolean checkUserNameUnique(SysUserBean user) {
        return false;
    }

    @Override
    public boolean checkPhoneUnique(SysUserBean user) {
        return false;
    }

    @Override
    public boolean checkEmailUnique(SysUserBean user) {
        return false;
    }

    @Override
    public void checkUserAllowed(SysUserBean user) {

    }

    @Override
    public void checkUserDataScope(String userId) {

    }

    @Override
    public int insertUser(SysUserBean user) {
        return 0;
    }

    @Override
    public boolean registerUser(SysUserBean user) {
        return false;
    }

    @Override
    public Integer updateUser(SysUserBean user) {
        return  sysUserRepository.saveByUserId(user);
    }

    @Override
    public void insertUserAuth(String userId, Long[] roleIds) {

    }

    @Override
    public int updateUserStatus(SysUserBean user) {
        return 0;
    }

    @Override
    public int updateUserProfile(SysUserBean user) {
        return 0;
    }

    @Override
    public boolean updateUserAvatar(String userName, String avatar) {
        return false;
    }

    @Override
    public int resetPwd(SysUserBean user) {
        return 0;
    }

    @Override
    public int resetUserPwd(String userName, String password) {
        return 0;
    }

    @Override
    public int deleteUserById(String userId) {
        return 0;
    }

    @Override
    public int deleteUserByIds(Long[] userIds) {
        return 0;
    }

    @Override
    public String importUser(List<SysUserBean> userList, Boolean isUpdateSupport, String operName) {
        return null;
    }
}
