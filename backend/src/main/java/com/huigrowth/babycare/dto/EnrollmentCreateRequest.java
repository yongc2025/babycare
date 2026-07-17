package com.huigrowth.babycare.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建宝宝入托档案请求
 */
@Data
public class EnrollmentCreateRequest {

    @NotNull(message = "宝宝ID不能为空")
    private Long babyId;

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    @NotNull(message = "班级ID不能为空")
    private Long classroomId;

    @JsonAlias({"entryDate", "admissionDate"})
    private LocalDate enrolledAt;

    @Size(max = 200, message = "过敏信息不能超过200个字符")
    private String allergyNotes;

    @Size(max = 300, message = "健康备注不能超过300个字符")
    private String medicalNotes;

    @Size(max = 300, message = "特殊照护备注不能超过300个字符")
    private String specialCareNotes;

    @Size(max = 30, message = "紧急联系人姓名不能超过30个字符")
    private String emergencyContactName;

    @Size(max = 20, message = "紧急联系人电话不能超过20个字符")
    private String emergencyContactPhone;
}
