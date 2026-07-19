package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 转班请求
 */
@Data
public class EnrollmentTransferRequest {

    @NotNull(message = "新班级ID不能为空")
    private Long newClassroomId;

    private String reason;
}
