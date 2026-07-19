package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 添加入托监护人请求
 */
@Data
public class EnrollmentGuardianRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private String relationship = "OTHER";

    private Boolean isPrimary = false;

    private String guardianPhone;

    private String remark;
}
