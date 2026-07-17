package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionCreateRequest {
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称最长50个字符")
    private String name;

    @NotBlank(message = "权限编码不能为空")
    @Pattern(regexp = "^[A-Z_]+$", message = "权限编码只能包含大写字母和下划线")
    @Size(max = 100, message = "权限编码最长100个字符")
    private String code;

    @Size(max = 255, message = "描述最长255个字符")
    private String description;

    @NotBlank(message = "资源类型不能为空")
    private String resourceType;

    private String method;
    private String urlPattern;
}
