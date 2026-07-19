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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 宝宝入托档案实体
 */
@Entity
@Table(name = "enrollments", indexes = {
    @Index(name = "idx_enrollment_baby", columnList = "baby_id"),
    @Index(name = "idx_enrollment_organization", columnList = "organization_id"),
    @Index(name = "idx_enrollment_classroom", columnList = "classroom_id"),
    @Index(name = "idx_enrollment_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_enrollment_baby_organization", columnNames = {"baby_id", "organization_id"})
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"baby", "organization", "classroom"})
@ToString(exclude = {"baby", "organization", "classroom"})
public class Enrollment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "baby_id", nullable = false)
    private Baby baby;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id", nullable = false)
    private Classroom classroom;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EnrollmentStatus status = EnrollmentStatus.PENDING;

    @Column(name = "enrolled_at")
    private LocalDate enrolledAt;

    @Size(max = 200, message = "过敏信息不能超过200个字符")
    @Column(name = "allergy_notes", length = 200, columnDefinition = "VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String allergyNotes;

    @Size(max = 300, message = "健康备注不能超过300个字符")
    @Column(name = "medical_notes", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String medicalNotes;

    @Size(max = 300, message = "特殊照护备注不能超过300个字符")
    @Column(name = "special_care_notes", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String specialCareNotes;

    @Size(max = 30, message = "紧急联系人姓名不能超过30个字符")
    @Column(name = "emergency_contact_name", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String emergencyContactName;

    @Size(max = 20, message = "紧急联系人电话不能超过20个字符")
    @Column(name = "emergency_contact_phone", length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String emergencyContactPhone;

    // 家长资料补充确认（T076）
    @Column(name = "parent_confirmed", nullable = false)
    private Boolean parentConfirmed = false;

    @Column(name = "parent_confirmed_at")
    private LocalDateTime parentConfirmedAt;

    // 审核相关
    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Size(max = 300, message = "驳回原因不能超过300个字符")
    @Column(name = "reject_reason", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String rejectReason;

    // 退托相关
    @Column(name = "withdrawn_at")
    private LocalDate withdrawnAt;

    @Size(max = 300, message = "退托原因不能超过300个字符")
    @Column(name = "withdraw_reason", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String withdrawReason;

    // 转班记录
    @Column(name = "previous_classroom_id")
    private Long previousClassroomId;

    @Size(max = 300, message = "转班原因不能超过300个字符")
    @Column(name = "transfer_reason", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String transferReason;

    public enum EnrollmentStatus {
        PENDING("待入托"),
        HEALTH_CHECK("保健审核中"),
        ACTIVE("在托"),
        SUSPENDED("暂停"),
        REJECTED("已驳回"),
        WITHDRAWN("退托");

        private final String description;

        EnrollmentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
