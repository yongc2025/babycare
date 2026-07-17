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
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "meal_intake_records", indexes = {
    @Index(name = "idx_meal_intake_meal_plan", columnList = "meal_plan_id"),
    @Index(name = "idx_meal_intake_enrollment", columnList = "enrollment_id")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"mealPlan", "enrollment", "recordedBy"})
@ToString(exclude = {"mealPlan", "enrollment", "recordedBy"})
public class MealIntakeRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "intake_level", nullable = false, length = 20)
    private IntakeLevel intakeLevel = IntakeLevel.MOST;

    @Column(name = "allergy_reaction", nullable = false)
    private Boolean allergyReaction = false;

    @Size(max = 300, message = "反应说明不能超过300个字符")
    @Column(name = "reaction_notes", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String reactionNotes;

    @Size(max = 300, message = "备注不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    public enum IntakeLevel {
        ALL("全部"),
        MOST("大部分"),
        HALF("一半"),
        LESS("少量"),
        NONE("拒食");

        private final String description;

        IntakeLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
