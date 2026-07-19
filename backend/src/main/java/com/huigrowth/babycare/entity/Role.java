package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统角色（sys_role）
 * 定义平台级角色，如系统管理员、老板、园长、教师、保健员等。
 */
@Entity
@Table(name = "sys_role", indexes = {
    @Index(name = "idx_sys_role_code", columnList = "code", unique = true),
    @Index(name = "idx_sys_role_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class Role extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RoleType type = RoleType.SYSTEM;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RoleStatus status = RoleStatus.ACTIVE;

    public enum RoleType {
        SYSTEM("系统角色"),
        CUSTOM("自定义角色");

        private final String description;
        RoleType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public enum RoleStatus {
        ACTIVE("启用"),
        DISABLED("禁用");

        private final String description;
        RoleStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
