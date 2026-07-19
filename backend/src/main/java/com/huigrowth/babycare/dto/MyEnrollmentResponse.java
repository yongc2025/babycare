package com.huigrowth.babycare.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 我的入托档案（家长视角）
 */
@Data
public class MyEnrollmentResponse {

    private Long enrollmentId;
    private String status;
    private String statusDescription;
    private LocalDate enrolledAt;

    // 宝宝信息
    private Long babyId;
    private String babyName;
    private String babyGender;
    private LocalDate babyBirthday;

    // 机构信息
    private Long organizationId;
    private String organizationName;

    // 班级信息
    private Long classroomId;
    private String classroomName;

    // 宝宝补充信息
    private String babyIdCard;
    private String babyBirthCertificateNo;

    // 监护人信息
    private Long guardianId;
    private String relationship;
    private String relationshipDescription;
    private String guardianIdCard;
    private String guardianOccupation;
    private String guardianPhone;
    private Boolean isPrimary;

    // 紧急联系人
    private String emergencyContactName;
    private String emergencyContactPhone;

    // 家长确认状态
    private Boolean parentConfirmed;
    private LocalDateTime parentConfirmedAt;

    private LocalDateTime createdAt;
}
