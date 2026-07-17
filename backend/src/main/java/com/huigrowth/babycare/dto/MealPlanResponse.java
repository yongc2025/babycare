package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.MealPlan;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class MealPlanResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private LocalDate mealDate;
    private MealPlan.MealType mealType;
    private String mealTypeDescription;
    private String title;
    private String foodItems;
    private String allergenNotes;
    private String nutritionNotes;
    private MealPlan.MealPlanStatus status;
    private String statusDescription;
    private Long createdById;
    private String createdByName;
    private List<MealIntakeResponse> intakeRecords;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
