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
@Table(name = "care_records", indexes = {
    @Index(name = "idx_care_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_care_date", columnList = "record_date"),
    @Index(name = "idx_care_type", columnList = "type")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "recordedBy"})
@ToString(exclude = {"enrollment", "recordedBy"})
public class CareRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "record_time", nullable = false)
    private LocalDateTime recordTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private CareType type;

    @Size(max = 100, message = "记录值不能超过100个字符")
    @Column(name = "value_text", length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String valueText;

    @Column(name = "amount")
    private Double amount;

    @Size(max = 20, message = "单位不能超过20个字符")
    @Column(name = "unit", length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String unit;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Size(max = 300, message = "照护备注不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    @Size(max = 30, message = "数据来源不能超过30个字符")
    @Column(name = "source", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String source = "MANUAL";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    /** 是否补录 */
    @Column(name = "is_backfill")
    private Boolean isBackfill = false;

    /** 补录原因 */
    @Size(max = 300, message = "补录原因不能超过300个字符")
    @Column(name = "backfill_reason", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String backfillReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "backfilled_by")
    private User backfilledBy;

    @Column(name = "backfilled_at")
    private LocalDateTime backfilledAt;

    public enum CareType {
        FEEDING("喂养"),
        WATER("饮水"),
        SLEEP("睡眠"),
        TOILET("如厕"),
        TEMPERATURE("体温"),
        MOOD("情绪"),
        ACTIVITY("活动");

        private final String description;

        CareType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
