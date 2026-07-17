package com.huigrowth.babycare.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicationAdministrationResponse {
    private Long id;
    private Long medicationRequestId;
    private LocalDateTime administeredAt;
    private String actualDosage;
    private Boolean reactionObserved;
    private String remark;
    private Long administeredById;
    private String administeredByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
