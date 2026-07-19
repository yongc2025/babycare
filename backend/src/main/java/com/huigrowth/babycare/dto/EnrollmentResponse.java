package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Enrollment;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宝宝入托档案响应
 */
@Data
public class EnrollmentResponse {
    private Long id;
    private Long babyId;
    private String babyName;
    private Baby.Gender babyGender;
    private LocalDate babyBirthday;
    private Long familyId;
    private Long organizationId;
    private String organizationName;
    private Long classroomId;
    private String classroomName;
    private Enrollment.EnrollmentStatus status;
    private String statusDescription;
    private LocalDate enrolledAt;
    private String allergyNotes;
    private String medicalNotes;
    private String specialCareNotes;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private Boolean parentConfirmed;
    private LocalDateTime parentConfirmedAt;

    // 宝宝补充信息
    private String babyIdCard;
    private String babyBirthCertificateNo;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
