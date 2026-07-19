package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 园长任命请求
 */
@Data
public class DirectorAppointRequest {

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    @NotNull(message = "用户ID不能为空")
    private Long userId;
}
