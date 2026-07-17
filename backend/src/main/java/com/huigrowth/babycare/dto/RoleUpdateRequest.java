package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleUpdateRequest {
    @Size(max = 50, message = "角色名称最长50个字符")
    private String name;

    @Size(max = 255, message = "描述最长255个字符")
    private String description;

    private String status;
}
