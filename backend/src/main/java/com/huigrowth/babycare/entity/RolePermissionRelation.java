package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-权限关联（sys_role_permission）
 * 控制角色拥有的接口/按钮权限。
 */
@Entity
@Table(name = "sys_role_permission", indexes = {
    @Index(name = "idx_sys_role_perm_role", columnList = "role_id"),
    @Index(name = "idx_sys_role_perm_perm", columnList = "permission_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_sys_role_permission", columnNames = {"role_id", "permission_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePermissionRelation extends BaseEntity {

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "permission_id", nullable = false)
    private Long permissionId;
}
