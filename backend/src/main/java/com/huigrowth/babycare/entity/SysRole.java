package com.huigrowth.babycare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RBAC角色实体。
 * 用于区分系统登录角色与机构业务岗位。
 */
@Entity
@Table(name = "sys_role")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysRole extends BaseEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(nullable = false)
    private Boolean enabled = true;
}
