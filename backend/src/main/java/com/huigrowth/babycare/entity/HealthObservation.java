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
@Table(name = "health_observations", indexes = {
    @Index(name = "idx_health_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_health_date", columnList = "observation_date"),
    @Index(name = "idx_health_type", columnList = "type"),
    @Index(name = "idx_health_abnormal", columnList = "abnormal")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "recordedBy"})
@ToString(exclude = {"enrollment", "recordedBy"})
public class HealthObservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "observation_date", nullable = false)
    private LocalDate observationDate;

    @Column(name = "observation_time", nullable = false)
    private LocalDateTime observationTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ObservationType type;

    @Column(name = "temperature")
    private Double temperature;

    @Size(max = 100, message = "一摸结果不能超过100个字符")
    @Column(name = "touch_status", length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String touchStatus;

    @Size(max = 100, message = "二看结果不能超过100个字符")
    @Column(name = "look_status", length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String lookStatus;

    @Size(max = 100, message = "三问结果不能超过100个字符")
    @Column(name = "ask_status", length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String askStatus;

    @Size(max = 100, message = "四查结果不能超过100个字符")
    @Column(name = "check_status", length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String checkStatus;

    @Size(max = 300, message = "症状描述不能超过300个字符")
    @Column(name = "symptoms", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String symptoms;

    @Size(max = 300, message = "处理建议不能超过300个字符")
    @Column(name = "action_taken", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String actionTaken;

    @Column(name = "abnormal", nullable = false)
    private Boolean abnormal = false;

    @Column(name = "follow_up_required", nullable = false)
    private Boolean followUpRequired = false;

    @Size(max = 30, message = "数据来源不能超过30个字符")
    @Column(name = "source", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String source = "MANUAL";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    public enum ObservationType {
        MORNING_CHECK("晨检"),
        NOON_CHECK("午检"),
        FULL_DAY_OBSERVATION("全日观察");

        private final String description;

        ObservationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
