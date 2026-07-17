package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建请假申请请求
 */
@Data
public class LeaveRequestCreateRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    private String type;

    @Size(max = 300, message = "请假原因不能超过300个字符")
    private String reason;
}
