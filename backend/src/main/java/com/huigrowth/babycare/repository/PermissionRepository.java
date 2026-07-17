package com.huigrowth.babycare.repository;

import com.huigrowth.babycare.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    List<Permission> findByStatus(Permission.PermissionStatus status);

    @Query("SELECT p FROM Permission p JOIN RolePermissionRelation rp ON p.id = rp.permissionId WHERE rp.roleId = ?1")
    List<Permission> findByRoleId(Long roleId);

    @Query("SELECT p FROM Permission p JOIN RolePermissionRelation rp ON p.id = rp.permissionId " +
           "JOIN UserRoleRelation ur ON rp.roleId = ur.roleId WHERE ur.userId = ?1")
    List<Permission> findByUserId(Long userId);
}
