package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.SysRoleBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface SysRoleRepository extends JpaRepository<SysRoleBean, String> {
    @Query(value = "SELECT DISTINCT r.*" +
            "FROM sys_role r " +
            "LEFT JOIN sys_user_role ur ON ur.role_id = r.role_id " +
            "LEFT JOIN sys_user u ON u.user_id = ur.user_id " +
            "WHERE r.is_del = '0' AND r.status = '0' AND ur.user_id = :userId",
            nativeQuery = true)
    List<SysRoleBean> selectRoleByUserId(@Param("userId") String userId);

    @Query(value = "SELECT * FROM sys_role WHERE is_del = '0' AND status = '0' ORDER BY role_sort", nativeQuery = true)
    List<SysRoleBean> selectRoleAll();
}
