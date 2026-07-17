package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class RolePermissionAssignRequest {
    @NotNull(message = "角色ID不能为空")
    private Long roleId;

    private List<Long> permissionIds;
}
