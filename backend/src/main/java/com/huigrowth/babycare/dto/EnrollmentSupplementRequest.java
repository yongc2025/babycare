package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 家长补充入托资料请求（T076）
 */
@Data
public class EnrollmentSupplementRequest {

    // ========== 宝宝信息 ==========
    @Size(max = 18, message = "宝宝身份证号不能超过18个字符")
    private String babyIdCard;

    @Size(max = 30, message = "出生证编号不能超过30个字符")
    private String babyBirthCertificateNo;

    // ========== 监护人信息 ==========
    @Size(max = 18, message = "监护人身份证号不能超过18个字符")
    private String guardianIdCard;

    @Size(max = 50, message = "监护人职业不能超过50个字符")
    private String guardianOccupation;

    @Size(max = 20, message = "监护人电话不能超过20个字符")
    private String guardianPhone;

    // ========== 健康与紧急联系信息 ==========
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
