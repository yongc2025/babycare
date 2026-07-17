package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建机构员工请求
 */
@Data
public class StaffCreateRequest {

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotBlank(message = "员工角色不能为空")
    private String role;
}
