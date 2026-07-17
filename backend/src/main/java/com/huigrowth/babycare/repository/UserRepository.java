package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问层
 * 
 * @author HuiGrowth Team
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 通过用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 通过邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 通过手机号查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 查找启用的用户
     */
    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.enabled = true")
    Optional<User> findByUsernameAndEnabled(String username);

    /**
     * 通过邮箱或用户名查找用户
     */
    @Query("SELECT u FROM User u WHERE u.email = ?1 OR u.username = ?1")
    Optional<User> findByEmailOrUsername(String emailOrUsername);
}
