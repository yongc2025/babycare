package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.LeaveRequest;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 幼儿请假申请响应
 */
@Data
public class LeaveRequestResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveRequest.LeaveType type;
    private String typeDescription;
    private LeaveRequest.LeaveStatus status;
    private String statusDescription;
    private String reason;
    private Long requestedById;
    private String requestedByName;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
