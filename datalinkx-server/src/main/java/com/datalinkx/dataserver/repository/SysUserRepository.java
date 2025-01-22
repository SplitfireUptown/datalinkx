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
    /**
     * 通过用户名查询用户
     *
     * @param userName
     * @return
     */
    @Query(value = "select * from SYS_USER where user_name = :userName AND status = 0 AND is_del = 0", nativeQuery = true)
    SysUserBean selectUserByUserName(String userName);

    /**
     * 查询用户信息列表
     *
     * @return
     */
    @Query(value = "select * from SYS_USER where status = 0 AND is_del = 0", nativeQuery = true)
    List<SysUserBean> findAll();

    /**
     * 查询角色下的用户
     *
     * @param roleId
     * @return {@link List<SysUserBean> }
     */
    @Query(value = "select SYS_USER.* from SYS_USER,SYS_USER_ROLE where SYS_USER.user_id = SYS_USER_ROLE.user_id AND SYS_USER_ROLE.role_id = :roleId AND SYS_USER.status = 0 AND SYS_USER.is_del = 0", nativeQuery = true)
    List<SysUserBean> selectUserListByRoleId(String roleId);

    /**
     * 更新用户信息
     *
     * @param user 用户
     * @return {@link Integer }
     */
    @Transactional
    @Modifying
    @Query(value = "update SYS_USER set nick_name = :#{#user.nickName}, email = :#{#user.email}, remark = :#{#user.remark} where user_id = :#{#user.userId} AND status = 0 AND is_del = 0", nativeQuery = true)
    Integer saveByUserId(@Param("user") SysUserBean user);

    /**
     * 按id查询用户
     *
     * @param userId 用户id
     * @return {@link SysUserBean }
     */
    @Query(value = "select * from SYS_USER where user_id = :userId AND status = 0 AND is_del = 0", nativeQuery = true)
    SysUserBean selectUserById(String userId);

    /**
     * 更新头像
     *
     * @param userId 用户id
     * @param avatar 头像
     * @return {@link Integer }
     */
    @Transactional
    @Modifying
    @Query(value = "update SYS_USER set avatar = :avatar where user_id = :userId AND is_del = 0", nativeQuery = true)
    Integer updateAvatar(@Param("userId") String userId, @Param("avatar") String avatar);

    /**
     * 修改密码
     *
     * @param userName
     * @param password
     */
    @Transactional
    @Modifying
    @Query(value = "update SYS_USER set password = :password,password_level = :passwordLevel where user_name = :userName AND status = 0 AND is_del = 0", nativeQuery = true)
    Integer resetUserPwd(@Param("userName") String userName, @Param("password") String password, @Param("passwordLevel") String passwordLevel);
}
