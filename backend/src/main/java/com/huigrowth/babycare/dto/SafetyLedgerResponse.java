package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.SafetyLedger;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SafetyLedgerResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private Long relatedIncidentId;
    private String relatedIncidentTitle;
    private LocalDate ledgerDate;
    private SafetyLedger.LedgerType ledgerType;
    private String ledgerTypeDescription;
    private String title;
    private String content;
    private String location;
    private String responsiblePerson;
    private LocalDateTime dueAt;
    private LocalDateTime completedAt;
    private SafetyLedger.LedgerStatus status;
    private String statusDescription;
    private String handleRemark;
    private Long createdById;
    private String createdByName;
    private Long handledById;
    private String handledByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
