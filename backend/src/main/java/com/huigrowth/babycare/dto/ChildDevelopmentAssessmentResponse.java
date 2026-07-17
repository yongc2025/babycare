package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.ChildDevelopmentAssessment;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ChildDevelopmentAssessmentResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long organizationId;
    private String organizationName;
    private Long classroomId;
    private String classroomName;
    private LocalDate assessmentDate;
    private Integer childAgeMonths;
    private ChildDevelopmentAssessment.AssessmentMode assessmentMode;
    private String assessmentModeDescription;
    private String title;
    private Integer grossMotorScore;
    private Integer fineMotorScore;
    private Integer languageScore;
    private Integer cognitiveScore;
    private Integer socialEmotionalScore;
    private Integer healthScore;
    private Integer scienceScore;
    private Integer artScore;
    private Integer maxScore;
    private ChildDevelopmentAssessment.DevelopmentLevel overallLevel;
    private String overallLevelDescription;
    private String summary;
    private String recommendation;
    private String radarData;
    private Long assessedById;
    private String assessedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
