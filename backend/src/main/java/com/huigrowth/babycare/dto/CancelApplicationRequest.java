package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 取消申请请求 DTO
 */
@Data
public class CancelApplicationRequest {

    @NotBlank(message = "申请类型不能为空")
    private String applicationType; // LEAVE, MEDICATION, PICKUP

    @NotNull(message = "申请ID不能为空")
    private Long applicationId;

    private String reason;
}
