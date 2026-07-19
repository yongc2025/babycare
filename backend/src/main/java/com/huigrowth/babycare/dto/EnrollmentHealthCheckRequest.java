package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 入托保健审核请求
 */
@Data
public class EnrollmentHealthCheckRequest {

    @NotNull(message = "审核结果不能为空")
    private Boolean passed;

    private String remark;
}
