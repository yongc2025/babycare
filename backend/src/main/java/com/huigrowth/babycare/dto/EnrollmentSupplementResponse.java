package com.huigrowth.babycare.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 家长补充资料状态响应（T076）
 */
@Data
public class EnrollmentSupplementResponse {

    // 入托档案信息
    private Long enrollmentId;
    private String status;
    private String statusDescription;

    // 宝宝信息填写状态
    private boolean babyInfoFilled;       // 已填身份证号和出生证
    private String babyIdCard;
    private String babyBirthCertificateNo;
    private String babyName;
    private String babyGender;
    private String babyBirthday;

    // 监护人信息填写状态
    private boolean guardianInfoFilled;   // 已填身份证号和职业
    private String guardianIdCard;
    private String guardianOccupation;
    private String guardianPhone;
    private String guardianRelationship;

    // 健康与紧急联系信息填写状态
    private boolean healthInfoFilled;     // 已填过敏、健康备注、紧急联系人等
    private String allergyNotes;
    private String medicalNotes;
    private String specialCareNotes;
    private String emergencyContactName;
    private String emergencyContactPhone;

    // 综合状态
    private boolean allFilled;            // 所有必填已填
    private Boolean parentConfirmed;
    private LocalDateTime parentConfirmedAt;
}
