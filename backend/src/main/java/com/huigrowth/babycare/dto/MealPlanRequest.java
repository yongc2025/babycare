package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MealPlanRequest {

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    @NotNull(message = "食谱日期不能为空")
    private LocalDate mealDate;

    @NotBlank(message = "餐次类型不能为空")
    private String mealType;

    @NotBlank(message = "餐次标题不能为空")
    private String title;

    private String foodItems;
    private String allergenNotes;
    private String nutritionNotes;
    private String status;
}
