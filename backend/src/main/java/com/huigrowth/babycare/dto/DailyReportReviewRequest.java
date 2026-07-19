package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 日报审核请求
 */
@Data
public class DailyReportReviewRequest {

    @Size(max = 500, message = "审核意见不能超过500个字符")
    private String reason;
}
