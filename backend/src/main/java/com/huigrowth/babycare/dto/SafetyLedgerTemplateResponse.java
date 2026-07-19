package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.SafetyLedger;
import com.huigrowth.babycare.entity.SafetyLedgerTemplate;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SafetyLedgerTemplateResponse {
    private Long id;
    private Long organizationId;
    private String ledgerType;
    private String ledgerTypeDescription;
    private String frequency;
    private String frequencyDescription;
    private Integer dayOfWeek;
    private Integer dayOfMonth;
    private String title;
    private String location;
    private String responsiblePerson;
    private String content;
    private Boolean isActive;
    private LocalDate lastGeneratedAt;
    private LocalDate nextGenerateDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
