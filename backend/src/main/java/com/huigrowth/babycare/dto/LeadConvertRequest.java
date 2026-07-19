package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 招生线索转为入托档案请求
 */
@Data
public class LeadConvertRequest {

    @NotNull(message = "目标班级不能为空")
    private Long classroomId;

    private LocalDate enrolledAt;

    private String allergyNotes;

    private String medicalNotes;

    private String specialCareNotes;

    private String emergencyContactName;

    private String emergencyContactPhone;
}
