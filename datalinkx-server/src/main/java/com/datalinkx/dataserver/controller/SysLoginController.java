package com.datalinkx.dataserver.controller;

import com.datalinkx.common.constants.MetaConstants.CommonConstant;
import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.domain.SysMenuBean;
import com.datalinkx.dataserver.bean.domain.SysUserBean;
import com.datalinkx.dataserver.bean.model.LoginBody;
import com.datalinkx.dataserver.bean.model.LoginUser;
import com.datalinkx.dataserver.bean.vo.RouterVo;
import com.datalinkx.dataserver.service.ISysMenuService;
import com.datalinkx.dataserver.service.SysLoginService;
import com.datalinkx.dataserver.service.SysPermissionService;
import com.datalinkx.dataserver.service.TokenService;
import com.datalinkx.dataserver.utils.RSAEncrypt;
import com.datalinkx.dataserver.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * 登录验证
 *
 * @author ruoyi
 */
@RestController
public class SysLoginController {
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;

    /**
     * 登录方法
     *
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public WebResult<HashMap<String, String>> login(@RequestBody LoginBody loginBody) throws Exception {
        loginBody.setPassword(RSAEncrypt.decrypt(loginBody.getPassword()));
        // 生成令牌
        String token = loginService.login(loginBody.getUsername(), loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        HashMap<String, String> tokenMap = new HashMap<>();
        tokenMap.put(CommonConstant.TOKEN, token);
        return WebResult.of(tokenMap);
    }

    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/user/info")
    public WebResult<HashMap<String, Object>> getInfo() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        SysUserBean user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        if (!loginUser.getPermissions().equals(permissions)) {
            loginUser.setPermissions(permissions);
            tokenService.refreshToken(loginUser);
        }
        user.setPassword(null);
        HashMap<String, Object> resultMap = new HashMap<>();
        resultMap.put("user", user);
        resultMap.put("roles", roles);
        resultMap.put("permissions", permissions);
        return WebResult.of(resultMap);
    }

    /**
     * 获取路由信息
     *
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public WebResult<List<RouterVo>> getRouters() {
        Long userId = SecurityUtils.getUserId();
        List<SysMenuBean> menus = menuService.selectMenuTreeByUserId(userId);

        return WebResult.of(menuService.buildMenus(menus));
    }

    /**
     * 获取公钥
     */
    @GetMapping("getPubKey")
    public WebResult<String> getPubKey() {
        return WebResult.of(RSAEncrypt.getPubKey());
    }
}
