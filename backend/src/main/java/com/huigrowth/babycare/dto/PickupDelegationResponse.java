package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.PickupDelegation;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PickupDelegationResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private LocalDate pickupDate;
    private String pickupPersonName;
    private String pickupRelationship;
    private String pickupPhone;
    private String reason;
    private PickupDelegation.DelegationStatus status;
    private String statusDescription;
    private String pickupCode;
    private Long requestedById;
    private String requestedByName;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
