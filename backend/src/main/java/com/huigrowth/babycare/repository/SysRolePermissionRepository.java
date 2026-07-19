package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.SysRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 角色权限关联数据访问。
 */
public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, Long> {

    List<SysRolePermission> findByRoleId(Long roleId);

    void deleteByRoleId(Long roleId);
}
