package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.SysUserRoleBean;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SysUserRoleRepository extends JpaRepository<SysUserRoleBean, String> {
}
