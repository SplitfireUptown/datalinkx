package com.datalinkx.dataserver.bean.domain;

import java.io.Serializable;
import java.util.Objects;

public class SysRoleMenuId implements Serializable {
    private String roleId;
    private String menuId;

    // 必须重写 equals 和 hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SysRoleMenuId that = (SysRoleMenuId) o;
        return Objects.equals(roleId, that.roleId) && Objects.equals(menuId, that.menuId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleId, menuId);
    }

    // Getters and Setters
}
