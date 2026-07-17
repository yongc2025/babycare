package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 请假审批请求
 */
@Data
public class LeaveRequestReviewRequest {

    @Size(max = 300, message = "审批备注不能超过300个字符")
    private String reviewRemark;
}
