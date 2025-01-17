package com.datalinkx.dataserver.controller;

import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.domain.SysMenuBean;
import com.datalinkx.dataserver.repository.SysMenuRepository;
import com.datalinkx.dataserver.service.ISysMenuService;
import com.datalinkx.dataserver.utils.SecurityUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.HashMap;
import java.util.List;

import static com.datalinkx.common.utils.ObjectUtils.getNullPropertyNames;

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
    @Autowired
    private SysMenuRepository sysMenuRepository;

    /**
     * 获取菜单列表
     *
     * @return
     * @throws Exception
     */
    @GetMapping("/list")
    public WebResult<HashMap<String, List<SysMenuBean>>> selectMenuList() {
        List<SysMenuBean> menus = null;
        if (SecurityUtils.isAdmin(SecurityUtils.getUserId())) {
            menus = sysMenuRepository.selectMenuTreeAll();
        } else {
            menus = menuService.selectMenuList(SecurityUtils.getUserId());
        }
        HashMap<String, List<SysMenuBean>> resultMap = new HashMap<>();
        resultMap.put("menus", menus);
        return WebResult.of(resultMap);
    }

    /**
     * 新增|更新菜单
     *
     * @param menu
     */
    @PostMapping("/update")
    public WebResult<HashMap<String, Integer>> updateMenu(@RequestBody SysMenuBean menu) {
        int count = 0;
        if (menu.getMenuId() != null) {
            SysMenuBean sysMenuBean = sysMenuRepository.findById(menu.getMenuId()).orElse(new SysMenuBean());
            BeanUtils.copyProperties(menu, sysMenuBean, getNullPropertyNames(menu));
            count = menuService.updateMenu(sysMenuBean);
        } else {
            count = menuService.updateMenu(menu);
        }
        HashMap<String, Integer> resultMap = new HashMap<>();
        resultMap.put("count", count);
        return WebResult.of(resultMap);
    }

}
