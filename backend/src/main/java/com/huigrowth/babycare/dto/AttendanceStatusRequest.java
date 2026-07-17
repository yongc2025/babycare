package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 考勤状态记录请求
 */
@Data
public class AttendanceStatusRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    private LocalDate attendanceDate;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String remark;
}
