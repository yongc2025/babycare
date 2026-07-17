package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.AllergyTag;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AllergyTagResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private String allergen;
    private String reaction;
    private AllergyTag.AllergySeverity severity;
    private String severityDescription;
    private AllergyTag.AllergyStatus status;
    private String statusDescription;
    private String remark;
    private Long createdById;
    private String createdByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
