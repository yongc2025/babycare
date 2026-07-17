package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MealIntakeRequest {

    @NotNull(message = "食谱ID不能为空")
    private Long mealPlanId;

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    private String intakeLevel;
    private Boolean allergyReaction;
    private String reactionNotes;
    private String remark;
}
