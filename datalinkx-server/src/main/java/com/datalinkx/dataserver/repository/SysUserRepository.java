package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.SysUserBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SysUserRepository extends JpaRepository<SysUserBean, String> {
    @Query(value = "select * from SYS_USER where user_name = :userName", nativeQuery = true)
    SysUserBean selectUserByUserName(String userName);
}
