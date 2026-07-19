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

/**
 * 传染病防控记录
 */
@Entity
@Table(name = "infectious_diseases", indexes = {
    @Index(name = "idx_infectious_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_infectious_org", columnList = "organization_id"),
    @Index(name = "idx_infectious_classroom", columnList = "classroom_id"),
    @Index(name = "idx_infectious_status", columnList = "status"),
    @Index(name = "idx_infectious_onset", columnList = "onset_date")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "reportedBy"})
@ToString(exclude = {"enrollment", "reportedBy"})
public class InfectiousDisease extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Size(max = 80, message = "疾病名称不能超过80个字符")
    @Column(name = "disease_name", nullable = false, length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String diseaseName;

    @Size(max = 500, message = "症状描述不能超过500个字符")
    @Column(name = "symptoms", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String symptoms;

    @Column(name = "onset_date", nullable = false)
    private LocalDate onsetDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DiseaseStatus status = DiseaseStatus.SUSPECTED;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private DiseaseSeverity severity = DiseaseSeverity.MILD;

    @Column(name = "reported_at")
    private LocalDateTime reportedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by")
    private User reportedBy;

    @Column(name = "isolation_start")
    private LocalDate isolationStart;

    @Column(name = "isolation_end")
    private LocalDate isolationEnd;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Size(max = 500, message = "处理记录不能超过500个字符")
    @Column(name = "treatment_notes", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String treatmentNotes;

    @Column(name = "parent_notified", nullable = false)
    private Boolean parentNotified = false;

    @Size(max = 500, message = "密切接触者记录不能超过500个字符")
    @Column(name = "close_contacts", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String closeContacts;

    @Column(name = "classroom_alert_sent", nullable = false)
    private Boolean classroomAlertSent = false;

    @Size(max = 300, message = "备注不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    public enum DiseaseStatus {
        SUSPECTED("疑似"),
        CONFIRMED("确诊"),
        ISOLATED("隔离"),
        RECOVERED("康复"),
        RETURNED("复园");

        private final String description;

        DiseaseStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum DiseaseSeverity {
        MILD("轻"),
        MODERATE("中"),
        SEVERE("重");

        private final String description;

        DiseaseSeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
