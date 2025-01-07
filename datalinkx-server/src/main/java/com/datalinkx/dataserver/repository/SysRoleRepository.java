package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.SysRoleBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SysRoleRepository extends JpaRepository<SysRoleBean, String> {
    @Query(value = "SELECT DISTINCT r.role_id, r.role_name, r.role_key, r.role_sort, r.data_scope, " +
            "r.menu_check_strictly, r.dept_check_strictly, r.status, r.is_del, r.create_time, r.remark " +
            "FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON ur.role_id = r.role_id " +
            "LEFT JOIN sys_user u ON u.user_id = ur.user_id " +
            "WHERE r.is_del = '0' AND ur.user_id = :userId",
            nativeQuery = true)
    List<SysRoleBean> selectRolePermissionByUserId(@Param("userId") Long userId);
}
