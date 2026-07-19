package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 入托审核请求
 */
@Data
public class EnrollmentReviewRequest {

    @NotBlank(message = "审核操作不能为空")
    private String action; // APPROVE / REJECT

    private String reason; // 驳回原因
}
