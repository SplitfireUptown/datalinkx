package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.SysRoleMenuBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface SysRoleMenuRepository extends JpaRepository<SysRoleMenuBean, String> {

    /**
     * 删除角色菜单关联信息
     * @param roleId 角色ID
     * @param menuIds 菜单ID
     * @return 结果
     */
    @Transactional
    @Modifying
    @Query(value = "delete from sys_role_menu where role_id = :roleId",
            nativeQuery = true)
    int deleteAuthMenus(String roleId);
}
