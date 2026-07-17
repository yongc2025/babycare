package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MenuCreateRequest {
    @NotBlank(message = "菜单名称不能为空")
    @Size(max = 50, message = "菜单名称最长50个字符")
    private String name;

    @Size(max = 200, message = "路由最长200个字符")
    private String route;

    @Size(max = 50, message = "图标名最长50个字符")
    private String icon;

    private Long parentId;
    private Integer sortOrder;
    private String menuType;
    private Boolean visible;
    private String permissionCode;
}
