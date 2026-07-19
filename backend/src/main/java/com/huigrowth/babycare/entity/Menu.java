package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

/**
 * 系统菜单（sys_menu）
 * 定义前端侧栏菜单树，支持多级嵌套和角色可见性。
 */
@Entity
@Table(name = "sys_menu", indexes = {
    @Index(name = "idx_sys_menu_parent", columnList = "parent_id"),
    @Index(name = "idx_sys_menu_sort", columnList = "sort_order"),
    @Index(name = "idx_sys_menu_route", columnList = "route")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class Menu extends BaseEntity {

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "route", length = 200)
    private String route;

    @Column(name = "icon", length = 50)
    private String icon;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "menu_type", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MenuType menuType = MenuType.MENU;

    @Column(name = "is_visible", nullable = false)
    private Boolean visible = true;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MenuStatus status = MenuStatus.ACTIVE;

    @Column(name = "permission_code", length = 100)
    private String permissionCode;

    @Transient
    private List<Menu> children = new ArrayList<>();

    public enum MenuType {
        DIR("目录"),
        MENU("菜单"),
        BUTTON("按钮");

        private final String description;
        MenuType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public enum MenuStatus {
        ACTIVE("启用"),
        DISABLED("禁用");

        private final String description;
        MenuStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
