package com.huigrowth.babycare.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户与系统角色关联。
 */
@Entity
@Table(name = "sys_user_role")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysUserRole extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private SysRole role;
}
