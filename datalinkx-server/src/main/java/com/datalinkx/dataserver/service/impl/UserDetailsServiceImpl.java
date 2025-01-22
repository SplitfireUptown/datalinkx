package com.datalinkx.dataserver.service.impl;

import com.datalinkx.common.enums.UserStatus;
import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.dataserver.bean.domain.SysUserBean;
import com.datalinkx.dataserver.bean.model.LoginUser;
import com.datalinkx.dataserver.service.ISysUserService;
import com.datalinkx.dataserver.service.SysPasswordService;
import com.datalinkx.dataserver.service.SysPermissionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 用户验证处理
 *
 * @author ruoyi
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    private static final Logger log = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private SysPasswordService passwordService;

    @Autowired
    private SysPermissionService permissionService;

    @Override
    public UserDetails loadUserByUsername(String username) throws DatalinkXServerException {
        SysUserBean user = sysUserService.selectUserByUserName(username);
        if (Objects.isNull(user)) {
            log.info("登录用户：{} 不存在.", username);
            throw new BadCredentialsException("user.not.exists");
        } else if (UserStatus.DELETED.getCode().equals(user.getIsDel())) {
            log.info("登录用户：{} 已被删除.", username);
            throw new BadCredentialsException("user.password.delete");
        } else if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            log.info("登录用户：{} 已被停用.", username);
            throw new BadCredentialsException("user.blocked");
        }

        passwordService.validate(user);

        return createLoginUser(user);
    }

    public UserDetails createLoginUser(SysUserBean user) {
        return new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
    }
}
