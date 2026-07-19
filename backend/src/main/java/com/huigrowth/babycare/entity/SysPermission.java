package com.huigrowth.babycare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * RBAC权限资源模型。
 */
@Entity
@Table(name = "sys_permission")
@Data
@EqualsAndHashCode(callSuper = true)
public class SysPermission extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String type;

    @Column(length = 200)
    private String resource;

    @Column(nullable = false)
    private Boolean enabled = true;
}
