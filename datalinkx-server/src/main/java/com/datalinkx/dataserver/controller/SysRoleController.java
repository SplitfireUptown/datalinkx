package com.datalinkx.dataserver.controller;


import com.datalinkx.common.result.WebResult;
import com.datalinkx.dataserver.bean.domain.SysRoleBean;
import com.datalinkx.dataserver.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色信息
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/role")
public class SysRoleController {
    @Autowired
    private ISysRoleService roleService;

    @GetMapping("/list")
    public WebResult<List<SysRoleBean>> selectRoleList() {

        return WebResult.of(roleService.selectRoleAll());
    }
}
