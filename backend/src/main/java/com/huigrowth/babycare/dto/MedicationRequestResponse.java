package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.MedicationRequest;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MedicationRequestResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private String medicineName;
    private String dosage;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private String instructions;
    private MedicationRequest.MedicationStatus status;
    private String statusDescription;
    private Long requestedById;
    private String requestedByName;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
    private List<MedicationAdministrationResponse> administrations;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
