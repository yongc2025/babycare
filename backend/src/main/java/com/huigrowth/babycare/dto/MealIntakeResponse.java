package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.MealIntakeRecord;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MealIntakeResponse {
    private Long id;
    private Long mealPlanId;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private MealIntakeRecord.IntakeLevel intakeLevel;
    private String intakeLevelDescription;
    private Boolean allergyReaction;
    private String reactionNotes;
    private String remark;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
