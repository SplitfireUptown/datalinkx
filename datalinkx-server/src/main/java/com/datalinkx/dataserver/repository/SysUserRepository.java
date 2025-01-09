package com.datalinkx.dataserver.repository;

import com.datalinkx.dataserver.bean.domain.SysUserBean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SysUserRepository extends JpaRepository<SysUserBean, String> {
    @Query(value = "select * from SYS_USER where user_name = :userName AND status = 0", nativeQuery = true)
    SysUserBean selectUserByUserName(String userName);

    @Transactional
    @Modifying
    @Query(value = "update SYS_USER set nick_name = :#{#user.nickName}, email = :#{#user.email}, remark = :#{#user.remark} where user_id = :#{#user.userId} AND status = 0", nativeQuery = true)
    Integer saveByUserId(@Param("user") SysUserBean user);

    @Query(value = "select * from SYS_USER where user_id = :userId AND status = 0", nativeQuery = true)
    SysUserBean selectUserById(String userId);
}
