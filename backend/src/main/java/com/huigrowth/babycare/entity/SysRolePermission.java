package com.huigrowth.babycare.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色权限关联。
 */
@Entity
@Table(name = "sys_role_permission")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRolePermission extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private SysRole role;

    @ManyToOne
    @JoinColumn(name = "permission_id", nullable = false)
    private SysPermission permission;
}
