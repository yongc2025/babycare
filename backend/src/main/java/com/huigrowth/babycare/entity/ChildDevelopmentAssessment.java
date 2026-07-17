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
@Table(name = "child_development_assessments", indexes = {
    @Index(name = "idx_child_assessment_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_child_assessment_date", columnList = "assessment_date"),
    @Index(name = "idx_child_assessment_mode", columnList = "assessment_mode")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "assessedBy"})
@ToString(exclude = {"enrollment", "assessedBy"})
public class ChildDevelopmentAssessment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "assessment_date", nullable = false)
    private LocalDate assessmentDate;

    @Column(name = "child_age_months", nullable = false)
    private Integer childAgeMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "assessment_mode", nullable = false, length = 30)
    private AssessmentMode assessmentMode = AssessmentMode.TODDLER_MILESTONE;

    @NotBlank(message = "评估标题不能为空")
    @Size(max = 100, message = "评估标题不能超过100个字符")
    @Column(name = "title", nullable = false, length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;

    @Column(name = "gross_motor_score")
    private Integer grossMotorScore;

    @Column(name = "fine_motor_score")
    private Integer fineMotorScore;

    @Column(name = "language_score")
    private Integer languageScore;

    @Column(name = "cognitive_score")
    private Integer cognitiveScore;

    @Column(name = "social_emotional_score")
    private Integer socialEmotionalScore;

    @Column(name = "health_score")
    private Integer healthScore;

    @Column(name = "science_score")
    private Integer scienceScore;

    @Column(name = "art_score")
    private Integer artScore;

    @Column(name = "max_score", nullable = false)
    private Integer maxScore = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_level", nullable = false, length = 30)
    private DevelopmentLevel overallLevel = DevelopmentLevel.AGE_APPROPRIATE;

    @Size(max = 1000, message = "评估摘要不能超过1000个字符")
    @Column(name = "summary", length = 1000, columnDefinition = "VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String summary;

    @Size(max = 1000, message = "发展建议不能超过1000个字符")
    @Column(name = "recommendation", length = 1000, columnDefinition = "VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String recommendation;

    @Column(name = "radar_data", columnDefinition = "TEXT")
    private String radarData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assessed_by")
    private User assessedBy;

    public enum AssessmentMode {
        TODDLER_MILESTONE("月龄里程碑"),
        PRESCHOOL_DOMAIN("五大领域");

        private final String description;

        AssessmentMode(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum DevelopmentLevel {
        ADVANCED("超前"),
        AGE_APPROPRIATE("符合月龄"),
        NEEDS_SUPPORT("需要支持"),
        DELAY_RISK("可能滞后");

        private final String description;

        DevelopmentLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
