package com.huigrowth.babycare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "meal_plans", indexes = {
    @Index(name = "idx_meal_plan_organization_date", columnList = "organization_id, meal_date"),
    @Index(name = "idx_meal_plan_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"organization", "createdBy"})
@ToString(exclude = {"organization", "createdBy"})
public class MealPlan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "meal_date", nullable = false)
    private LocalDate mealDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 30)
    private MealType mealType = MealType.LUNCH;

    @NotBlank(message = "餐次标题不能为空")
    @Size(max = 80, message = "餐次标题不能超过80个字符")
    @Column(name = "title", nullable = false, length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;

    @Size(max = 500, message = "菜品内容不能超过500个字符")
    @Column(name = "food_items", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String foodItems;

    @Size(max = 300, message = "过敏提示不能超过300个字符")
    @Column(name = "allergen_notes", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String allergenNotes;

    @Size(max = 300, message = "营养说明不能超过300个字符")
    @Column(name = "nutrition_notes", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String nutritionNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MealPlanStatus status = MealPlanStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public enum MealType {
        BREAKFAST("早餐"),
        MORNING_SNACK("上午加餐"),
        LUNCH("午餐"),
        AFTERNOON_SNACK("下午加餐"),
        DINNER("晚餐");

        private final String description;

        MealType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum MealPlanStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布");

        private final String description;

        MealPlanStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
