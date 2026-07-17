package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统权限（sys_permission）
 * 定义接口级权限，用于方法级别的 @PreAuthorize 控制。
 */
@Entity
@Table(name = "sys_permission", indexes = {
    @Index(name = "idx_sys_permission_code", columnList = "code", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true)
public class Permission extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "code", nullable = false, length = 100, unique = true)
    private String code;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "resource_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ResourceType resourceType;

    @Column(name = "method", length = 20)
    private String method;

    @Column(name = "url_pattern", length = 255)
    private String urlPattern;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private PermissionStatus status = PermissionStatus.ACTIVE;

    public enum ResourceType {
        API("接口"),
        MENU("菜单"),
        BUTTON("按钮"),
        FIELD("字段");

        private final String description;
        ResourceType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public enum PermissionStatus {
        ACTIVE("启用"),
        DISABLED("禁用");

        private final String description;
        PermissionStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
