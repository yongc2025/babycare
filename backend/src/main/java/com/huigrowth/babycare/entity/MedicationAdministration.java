package com.huigrowth.babycare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "medication_administrations", indexes = {
    @Index(name = "idx_med_admin_request", columnList = "medication_request_id"),
    @Index(name = "idx_med_admin_time", columnList = "administered_at")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"medicationRequest", "administeredBy"})
@ToString(exclude = {"medicationRequest", "administeredBy"})
public class MedicationAdministration extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medication_request_id", nullable = false)
    private MedicationRequest medicationRequest;

    @Column(name = "administered_at", nullable = false)
    private LocalDateTime administeredAt;

    @Size(max = 80, message = "实际剂量不能超过80个字符")
    @Column(name = "actual_dosage", length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String actualDosage;

    @Column(name = "reaction_observed", nullable = false)
    private Boolean reactionObserved = false;

    @Size(max = 300, message = "执行备注不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "administered_by", nullable = false)
    private User administeredBy;
}
