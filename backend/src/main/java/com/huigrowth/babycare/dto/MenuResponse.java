package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.Menu;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class MenuResponse {
    private Long id;
    private String name;
    private String route;
    private String icon;
    private Long parentId;
    private Integer sortOrder;
    private String menuType;
    private Boolean visible;
    private String status;
    private String permissionCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<MenuResponse> children = new ArrayList<>();

    public static MenuResponse fromEntity(Menu menu) {
        MenuResponse r = new MenuResponse();
        r.setId(menu.getId());
        r.setName(menu.getName());
        r.setRoute(menu.getRoute());
        r.setIcon(menu.getIcon());
        r.setParentId(menu.getParentId());
        r.setSortOrder(menu.getSortOrder());
        r.setMenuType(menu.getMenuType() != null ? menu.getMenuType().name() : null);
        r.setVisible(menu.getVisible());
        r.setStatus(menu.getStatus() != null ? menu.getStatus().name() : null);
        r.setPermissionCode(menu.getPermissionCode());
        r.setCreatedAt(menu.getCreatedAt());
        r.setUpdatedAt(menu.getUpdatedAt());
        return r;
    }
}
