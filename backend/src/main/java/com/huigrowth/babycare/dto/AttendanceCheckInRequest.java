package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 到园记录请求
 */
@Data
public class AttendanceCheckInRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    private LocalDate attendanceDate;

    private Double temperature;

    @Size(max = 30, message = "数据来源不能超过30个字符")
    private String source;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String remark;
}
