package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.IncidentReport;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IncidentReportResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private IncidentReport.IncidentType type;
    private String typeDescription;
    private IncidentReport.IncidentSeverity severity;
    private String severityDescription;
    private IncidentReport.IncidentStatus status;
    private String statusDescription;
    private LocalDateTime occurredAt;
    private String location;
    private String title;
    private String description;
    private String handlingProcess;
    private String followUpPlan;
    private Boolean parentNotified;
    private LocalDateTime parentNotifiedAt;
    private Boolean parentConfirmed;
    private LocalDateTime parentConfirmedAt;
    private Long reportedById;
    private String reportedByName;
    private Long handledById;
    private String handledByName;
    private Long confirmedById;
    private String confirmedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
