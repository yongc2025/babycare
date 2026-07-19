package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * RBAC 权限资源数据访问
 */
public interface SysPermissionRepository extends JpaRepository<SysPermission, Long> {

    Optional<SysPermission> findByCode(String code);

    List<SysPermission> findByEnabledTrue();
}
