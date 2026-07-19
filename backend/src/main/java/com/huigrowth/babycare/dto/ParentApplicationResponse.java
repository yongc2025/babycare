package com.huigrowth.babycare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 家长申请统一响应 DTO
 * 聚合请假、用药委托、接送委托三种申请
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentApplicationResponse {

    private Long id;
    private String applicationType; // LEAVE, MEDICATION, PICKUP
    private String applicationTypeDescription;

    // 宝宝信息
    private Long babyId;
    private String babyName;
    private Long enrollmentId;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;

    // 通用字段
    private String status; // PENDING, APPROVED, REJECTED, CANCELLED
    private String statusDescription;
    private String reason;
    private String requestedByName;
    private String reviewedByName;
    private String reviewRemark;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    // 请假专用
    private LocalDate leaveStartDate;
    private LocalDate leaveEndDate;
    private String leaveType; // SICK, PERSONAL, OTHER
    private String leaveTypeDescription;

    // 用药委托专用
    private String medicineName;
    private String dosage;
    private String frequency;
    private LocalDate medicationStartDate;
    private LocalDate medicationEndDate;
    private String instructions;

    // 接送委托专用
    private LocalDate pickupDate;
    private String pickupPersonName;
    private String pickupRelationship;
    private String pickupPhone;
    private String pickupCode;
}
