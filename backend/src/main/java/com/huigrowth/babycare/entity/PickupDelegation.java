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
@Table(name = "pickup_delegations", indexes = {
    @Index(name = "idx_pickup_delegation_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_pickup_delegation_date", columnList = "pickup_date"),
    @Index(name = "idx_pickup_delegation_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "requestedBy", "reviewedBy"})
@ToString(exclude = {"enrollment", "requestedBy", "reviewedBy"})
public class PickupDelegation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "pickup_date", nullable = false)
    private LocalDate pickupDate;

    @Size(max = 30, message = "接送人姓名不能超过30个字符")
    @Column(name = "pickup_person_name", nullable = false, length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String pickupPersonName;

    @Size(max = 30, message = "接送关系不能超过30个字符")
    @Column(name = "pickup_relationship", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String pickupRelationship;

    @Size(max = 20, message = "接送人电话不能超过20个字符")
    @Column(name = "pickup_phone", length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String pickupPhone;

    @Size(max = 300, message = "委托原因不能超过300个字符")
    @Column(name = "reason", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private DelegationStatus status = DelegationStatus.PENDING;

    @Size(max = 20, message = "接送码不能超过20个字符")
    @Column(name = "pickup_code", length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String pickupCode;

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

    public enum DelegationStatus {
        PENDING("待审核"),
        APPROVED("已通过"),
        REJECTED("已拒绝"),
        CANCELLED("已取消");

        private final String description;

        DelegationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
