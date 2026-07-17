package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DailyReportGenerateRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    private LocalDate reportDate;

    @Size(max = 800, message = "老师评语不能超过800个字符")
    private String teacherComment;
}
