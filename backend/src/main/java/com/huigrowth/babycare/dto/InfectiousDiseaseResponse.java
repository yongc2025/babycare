package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.InfectiousDisease;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InfectiousDiseaseResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private String diseaseName;
    private String symptoms;
    private LocalDate onsetDate;
    private InfectiousDisease.DiseaseStatus status;
    private String statusDescription;
    private InfectiousDisease.DiseaseSeverity severity;
    private String severityDescription;
    private LocalDateTime reportedAt;
    private Long reportedById;
    private String reportedByName;
    private LocalDate isolationStart;
    private LocalDate isolationEnd;
    private LocalDate returnDate;
    private String treatmentNotes;
    private Boolean parentNotified;
    private String closeContacts;
    private Boolean classroomAlertSent;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
