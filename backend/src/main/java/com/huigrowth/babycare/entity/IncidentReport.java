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

import java.time.LocalDateTime;

@Entity
@Table(name = "incident_reports", indexes = {
    @Index(name = "idx_incident_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_incident_class_status", columnList = "status,severity"),
    @Index(name = "idx_incident_occurred_at", columnList = "occurred_at")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "reportedBy", "handledBy", "confirmedBy"})
@ToString(exclude = {"enrollment", "reportedBy", "handledBy", "confirmedBy"})
public class IncidentReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private IncidentType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 20)
    private IncidentSeverity severity = IncidentSeverity.LOW;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Size(max = 80, message = "发生地点不能超过80个字符")
    @Column(name = "location", length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String location;

    @Size(max = 120, message = "标题不能超过120个字符")
    @Column(name = "title", nullable = false, length = 120, columnDefinition = "VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;

    @Size(max = 1000, message = "事故描述不能超过1000个字符")
    @Column(name = "description", length = 1000, columnDefinition = "VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String description;

    @Size(max = 1000, message = "处理过程不能超过1000个字符")
    @Column(name = "handling_process", length = 1000, columnDefinition = "VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String handlingProcess;

    @Size(max = 500, message = "后续跟进不能超过500个字符")
    @Column(name = "follow_up_plan", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String followUpPlan;

    @Column(name = "parent_notified", nullable = false)
    private Boolean parentNotified = false;

    @Column(name = "parent_notified_at")
    private LocalDateTime parentNotifiedAt;

    @Column(name = "parent_confirmed", nullable = false)
    private Boolean parentConfirmed = false;

    @Column(name = "parent_confirmed_at")
    private LocalDateTime parentConfirmedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_by", nullable = false)
    private User reportedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confirmed_by")
    private User confirmedBy;

    public enum IncidentType {
        HEALTH_ABNORMAL("健康异常"),
        INJURY("意外伤害"),
        SAFETY_EVENT("安全事件"),
        BEHAVIOR("行为异常"),
        OTHER("其他");

        private final String description;

        IncidentType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum IncidentSeverity {
        LOW("低"),
        MEDIUM("中"),
        HIGH("高"),
        CRITICAL("紧急");

        private final String description;

        IncidentSeverity(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum IncidentStatus {
        OPEN("待处理"),
        PROCESSING("处理中"),
        CLOSED("已关闭");

        private final String description;

        IncidentStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
