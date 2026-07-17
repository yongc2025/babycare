package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户-角色关联（sys_user_role）
 * 支持一个用户拥有多个角色。
 */
@Entity
@Table(name = "sys_user_role", indexes = {
    @Index(name = "idx_sys_user_role_user", columnList = "user_id"),
    @Index(name = "idx_sys_user_role_role", columnList = "role_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_sys_user_role", columnNames = {"user_id", "role_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class UserRoleRelation extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "role_id", nullable = false)
    private Long roleId;
}
