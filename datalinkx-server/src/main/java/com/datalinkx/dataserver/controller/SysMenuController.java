package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.domain.SysMenuBean;
import com.datalinkx.dataserver.service.ISysMenuService;
import com.datalinkx.dataserver.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.List;

/**
 * 菜单信息
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/menu")
public class SysMenuController {
    @Autowired
    private ISysMenuService menuService;

    /**
     * 获取菜单列表
     */
    @GetMapping("/list")
    public WebResult<HashMap<String, List<SysMenuBean>>> selectMenuList() {
        List<SysMenuBean> menus = menuService.selectMenuList(SecurityUtils.getUserId());
        HashMap<String, List<SysMenuBean>> resultMap = new HashMap<>();
        resultMap.put("menus", menus);
        return WebResult.of(resultMap);
    }
}
