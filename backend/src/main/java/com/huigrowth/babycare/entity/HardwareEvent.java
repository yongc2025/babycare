package com.huigrowth.babycare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "hardware_events", indexes = {
    @Index(name = "idx_hardware_event_device", columnList = "device_id"),
    @Index(name = "idx_hardware_event_organization", columnList = "organization_id"),
    @Index(name = "idx_hardware_event_time", columnList = "event_time"),
    @Index(name = "idx_hardware_event_type", columnList = "event_type"),
    @Index(name = "idx_hardware_event_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"device", "organization", "classroom", "enrollment"})
@ToString(exclude = {"device", "organization", "classroom", "enrollment"})
public class HardwareEvent extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", nullable = false)
    private HardwareDevice device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id")
    private Enrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 40)
    private EventType eventType = EventType.RAW_MESSAGE;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "event_key", length = 120, columnDefinition = "VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String eventKey;

    @Column(name = "subject_ref", length = 120, columnDefinition = "VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String subjectRef;

    @Column(name = "confidence")
    private Double confidence;

    @Lob
    @Column(name = "payload", columnDefinition = "TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private EventStatus status = EventStatus.RECEIVED;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "process_remark", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String processRemark;

    public enum EventType {
        ATTENDANCE_CHECK_IN("到园识别"),
        ATTENDANCE_CHECK_OUT("离园识别"),
        HEALTH_MEASUREMENT("健康测量"),
        FACE_CAPTURE("人脸抓拍"),
        PICKUP_VERIFY("接送核验"),
        SAFETY_ALERT("安全告警"),
        RAW_MESSAGE("原始消息");

        private final String description;

        EventType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum EventStatus {
        RECEIVED("已接收"),
        IGNORED("已忽略"),
        PROCESSED("已处理"),
        FAILED("处理失败");

        private final String description;

        EventStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
