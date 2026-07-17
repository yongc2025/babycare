package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.AdmissionLead;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AdmissionLeadResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private Long intendedClassroomId;
    private String intendedClassroomName;
    private String childName;
    private String childGender;
    private LocalDate childBirthday;
    private String guardianName;
    private String guardianPhone;
    private AdmissionLead.LeadSource source;
    private String sourceDescription;
    private AdmissionLead.IntentionLevel intentionLevel;
    private String intentionLevelDescription;
    private AdmissionLead.LeadStatus status;
    private String statusDescription;
    private LocalDate preferredStartDate;
    private String remark;
    private Long reviewedById;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private String reviewRemark;
    private LocalDate trialStartDate;
    private LocalDate trialEndDate;
    private String trialFeedback;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
