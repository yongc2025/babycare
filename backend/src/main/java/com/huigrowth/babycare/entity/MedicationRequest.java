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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "medication_requests", indexes = {
    @Index(name = "idx_medication_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_medication_status", columnList = "status"),
    @Index(name = "idx_medication_date", columnList = "start_date,end_date")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "requestedBy", "reviewedBy"})
@ToString(exclude = {"enrollment", "requestedBy", "reviewedBy"})
public class MedicationRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Size(max = 80, message = "药品名称不能超过80个字符")
    @Column(name = "medicine_name", nullable = false, length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String medicineName;

    @Size(max = 80, message = "剂量不能超过80个字符")
    @Column(name = "dosage", length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String dosage;

    @Size(max = 120, message = "频次不能超过120个字符")
    @Column(name = "frequency", length = 120, columnDefinition = "VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String frequency;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Size(max = 300, message = "用药说明不能超过300个字符")
    @Column(name = "instructions", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String instructions;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MedicationStatus status = MedicationStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Size(max = 300, message = "审核备注不能超过300个字符")
    @Column(name = "review_remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String reviewRemark;

    public enum MedicationStatus {
        PENDING("待审核"),
        APPROVED("已通过"),
        REJECTED("已拒绝"),
        CANCELLED("已取消");

        private final String description;

        MedicationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
