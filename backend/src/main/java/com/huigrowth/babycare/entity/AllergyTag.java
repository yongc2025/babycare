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
@Table(name = "allergy_tags", indexes = {
    @Index(name = "idx_allergy_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_allergy_status", columnList = "status"),
    @Index(name = "idx_allergy_severity", columnList = "severity")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "createdBy"})
@ToString(exclude = {"enrollment", "createdBy"})
public class AllergyTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Size(max = 80, message = "过敏源不能超过80个字符")
    @Column(name = "allergen", nullable = false, length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String allergen;

    @Size(max = 300, message = "过敏反应不能超过300个字符")
    @Column(name = "reaction", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String reaction;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private AllergySeverity severity = AllergySeverity.MILD;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AllergyStatus status = AllergyStatus.ACTIVE;

    @Size(max = 300, message = "备注不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public enum AllergySeverity {
        MILD("轻微"),
        MODERATE("中等"),
        SEVERE("严重");

        private final String description;

        AllergySeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum AllergyStatus {
        ACTIVE("生效"),
        INACTIVE("停用");

        private final String description;

        AllergyStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
