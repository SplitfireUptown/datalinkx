package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.SysMenuBean;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysMenuRepository extends JpaRepository<SysMenuBean, String> {
    /**
     * 查询菜单
     *
     * @return 菜单列表
     */
    @Query(value = "SELECT DISTINCT m.menu_id, m.parent_id, m.menu_name, m.path, m.component, m.query, " +
            "m.route_name, m.visible, m.status, IFNULL(m.perms, '') AS perms, m.is_frame, " +
            "m.is_cache, m.menu_type, m.icon, m.order_num, m.create_time " +
            "FROM sys_menu m " +
            "WHERE m.menu_type IN ('M', 'C') AND m.status = 0 " +
            "ORDER BY m.parent_id, m.order_num",
            nativeQuery = true)
    List<SysMenuBean> selectMenuTreeAll();

    /**
     * 根据用户ID查询菜单
     *
     * @param userId 用户ID
     * @return 菜单列表
     */
    @Query(value = "SELECT DISTINCT m.menu_id, m.parent_id, m.menu_name, m.path, m.component, m.query, " +
            "m.route_name, m.visible, m.status, IFNULL(m.perms, '') AS perms, m.is_frame, " +
            "m.is_cache, m.menu_type, m.icon, m.order_num, m.create_time " +
            "FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "LEFT JOIN sys_role ro ON ur.role_id = ro.role_id " +
            "LEFT JOIN sys_user u ON ur.user_id = u.user_id " +
            "WHERE u.user_id = :userId AND m.menu_type IN ('M', 'C') AND m.status = 0 AND ro.status = 0 " +
            "ORDER BY m.parent_id, m.order_num",
            nativeQuery = true)
    List<SysMenuBean> selectMenuTreeByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID查询权限
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    @Query(value = "SELECT DISTINCT m.perms " +
            "FROM sys_menu m " +
            "LEFT JOIN sys_role_menu rm ON m.menu_id = rm.menu_id " +
            "LEFT JOIN sys_user_role ur ON rm.role_id = ur.role_id " +
            "LEFT JOIN sys_role r ON r.role_id = ur.role_id " +
            "WHERE m.status = '0' AND r.status = '0' AND ur.user_id = :userId",
            nativeQuery = true)
    List<String> selectMenuPermsByUserId(@Param("userId") Long userId);
}
