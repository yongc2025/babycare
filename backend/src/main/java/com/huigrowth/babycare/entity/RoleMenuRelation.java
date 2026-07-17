package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色-菜单关联（sys_role_menu）
 * 控制角色可见的菜单项。
 */
@Entity
@Table(name = "sys_role_menu", indexes = {
    @Index(name = "idx_sys_role_menu_role", columnList = "role_id"),
    @Index(name = "idx_sys_role_menu_menu", columnList = "menu_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_sys_role_menu", columnNames = {"role_id", "menu_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleMenuRelation extends BaseEntity {

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;
}
