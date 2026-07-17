package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.AttendanceRecord;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 幼儿考勤记录响应
 */
@Data
public class AttendanceResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private LocalDate attendanceDate;
    private AttendanceRecord.AttendanceStatus status;
    private String statusDescription;
    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private Double temperature;
    private String pickupPersonName;
    private String pickupRelationship;
    private String pickupPhone;
    private String source;
    private String remark;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
