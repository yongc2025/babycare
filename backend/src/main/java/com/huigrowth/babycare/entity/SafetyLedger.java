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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "safety_ledgers", indexes = {
    @Index(name = "idx_safety_ledger_organization_date", columnList = "organization_id, ledger_date"),
    @Index(name = "idx_safety_ledger_type", columnList = "ledger_type"),
    @Index(name = "idx_safety_ledger_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"organization", "relatedIncident", "createdBy", "handledBy"})
@ToString(exclude = {"organization", "relatedIncident", "createdBy", "handledBy"})
public class SafetyLedger extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "ledger_date", nullable = false)
    private LocalDate ledgerDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type", nullable = false, length = 40)
    private LedgerType ledgerType = LedgerType.OTHER;

    @NotBlank(message = "台账标题不能为空")
    @Size(max = 100, message = "台账标题不能超过100个字符")
    @Column(name = "title", nullable = false, length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;

    @Size(max = 1000, message = "台账内容不能超过1000个字符")
    @Column(name = "content", length = 1000, columnDefinition = "VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String content;

    @Size(max = 100, message = "位置不能超过100个字符")
    @Column(name = "location", length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String location;

    @Size(max = 50, message = "责任人不能超过50个字符")
    @Column(name = "responsible_person", length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String responsiblePerson;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LedgerStatus status = LedgerStatus.OPEN;

    @Size(max = 500, message = "处理备注不能超过500个字符")
    @Column(name = "handle_remark", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String handleRemark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_incident_id")
    private IncidentReport relatedIncident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;

    public enum LedgerType {
        DISINFECTION("消毒记录"),
        FOOD_SAMPLE("食品留样"),
        FACILITY_INSPECTION("设施巡检"),
        FIRE_SAFETY("消防台账"),
        SAFETY_EDUCATION("安全教育"),
        INCIDENT_FOLLOWUP("事故跟进"),
        OTHER("其他");

        private final String description;

        LedgerType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum LedgerStatus {
        OPEN("待处理"),
        PROCESSING("处理中"),
        CLOSED("已关闭"),
        OVERDUE("已逾期");

        private final String description;

        LedgerStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
