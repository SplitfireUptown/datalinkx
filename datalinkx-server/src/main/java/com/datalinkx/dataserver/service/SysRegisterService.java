package com.datalinkx.dataserver.service;

import com.datalinkx.common.exception.DatalinkXServerException;
import com.datalinkx.dataserver.bean.domain.SysUserBean;
import com.datalinkx.dataserver.bean.domain.SysUserRoleBean;
import com.datalinkx.dataserver.bean.model.RegisterBody;
import com.datalinkx.dataserver.repository.SysUserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

import static com.datalinkx.dataserver.utils.RSAEncrypt.decrypt;
import static com.datalinkx.dataserver.utils.SecurityUtils.encryptPassword;

/**
 * 注册校验方法
 *
 * @author ruoyi
 */
@Component
public class SysRegisterService {
    @Autowired
    private ISysUserService userService;
    @Autowired
    private SysUserRoleRepository sysUserRoleRepository;

    /**
     * 注册
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean register(RegisterBody user) throws Exception {
        SysUserBean sysUserBean = new SysUserBean();
        sysUserBean.setUserName(user.getUsername());
        sysUserBean.setNickName(user.getNickname());
        sysUserBean.setEmail(user.getEmail());
        sysUserBean.setPasswordLevel(user.getPasswordLevel());
        if (!userService.checkUserNameUnique(sysUserBean)) {
            throw new DatalinkXServerException("注册失败,用户名" + sysUserBean.getUserName() + "已经存在");
        } else {
            sysUserBean.setPassword(encryptPassword(decrypt(user.getPassword())));
            SysUserBean registerUser = userService.registerUser(sysUserBean);
            SysUserRoleBean sysUserRoleBean = new SysUserRoleBean();
            sysUserRoleBean.setUserId(registerUser.getUserId());
            sysUserRoleBean.setRoleId("2");
            SysUserRoleBean userRoleBean = sysUserRoleRepository.save(sysUserRoleBean);
            if (Objects.isNull(registerUser) || Objects.isNull(userRoleBean)) {
                throw new DatalinkXServerException("注册失败,请联系管理员");
            }
        }
        return true;
    }
}
