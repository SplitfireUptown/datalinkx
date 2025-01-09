package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.domain.SysRoleBean;
import com.datalinkx.dataserver.bean.domain.SysUserBean;
import com.datalinkx.dataserver.repository.SysRoleRepository;
import com.datalinkx.dataserver.service.ISysRoleService;
import com.datalinkx.dataserver.service.ISysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.datalinkx.dataserver.utils.SecurityUtils.getUserId;


/**
 * 用户信息
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/user")
public class SysUserController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    SysRoleRepository sysRoleRepository;

    /**
     * 根据用户编号获取详细信息
     */
    @GetMapping(value = {"/", "/{userId}"})
    public WebResult<HashMap<String, Object>> getInfo(@PathVariable(value = "userId", required = false) String userId) {
        HashMap<String, Object> resultMap = new HashMap<>();
        SysUserBean sysUserBean = new SysUserBean();
        ArrayList<SysRoleBean> sysRoleBean = new ArrayList<>();
        if (!Objects.isNull(userId)) {
            sysUserBean = userService.selectUserById(userId);
            sysRoleBean.addAll(sysRoleRepository.selectRoleByUserId(userId));
        } else {
            sysUserBean = userService.selectUserById(getUserId());
            sysRoleBean.addAll(sysRoleRepository.selectRoleByUserId(getUserId()));
        }
        resultMap.put("user", sysUserBean);
        resultMap.put("roles", sysRoleBean);
        return WebResult.of(resultMap);
    }
    
    /**
     * 修改用户
     */
    @PutMapping
    public WebResult<Integer> edit(@Validated @RequestBody SysUserBean user)
    {
//        if (!userService.checkUserNameUnique(user))
//        {
//            return WebResult.fail(new DatalinkXServerException("修改用户'" + user.getNickName() + "'失败，登录账号已存在"));
//        }
//        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
//        {
//            return WebResult.fail(new DatalinkXServerException("修改用户'" + user.getEmail() + "'失败，邮箱账号已存在"));
//        }
//        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
//        {
//            return WebResult.fail(new DatalinkXServerException("修改用户'" + user.getUserName() + "'失败，手机号码已存在"));
//        }
        user.setUpdateBy(getUserId());
        return WebResult.of(userService.updateUser(user));
    }
}
