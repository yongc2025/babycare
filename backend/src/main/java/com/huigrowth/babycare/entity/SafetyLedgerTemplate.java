package com.huigrowth.babycare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 安全台账周期任务模板
 * 定义消毒、巡检、消防等台账的自动生成规则
 */
@Entity
@Table(name = "safety_ledger_templates", indexes = {
    @Index(name = "idx_slt_org", columnList = "organization_id"),
    @Index(name = "idx_slt_org_active", columnList = "organization_id, is_active"),
    @Index(name = "idx_slt_next", columnList = "next_generate_date")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class SafetyLedgerTemplate extends BaseEntity {

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "ledger_type", nullable = false, length = 40)
    private SafetyLedger.LedgerType ledgerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "frequency", nullable = false, length = 20)
    private Frequency frequency;

    @Column(name = "day_of_week")
    private Integer dayOfWeek;

    @Column(name = "day_of_month")
    private Integer dayOfMonth;

    @NotBlank(message = "模板标题不能为空")
    @Size(max = 100, message = "模板标题不能超过100个字符")
    @Column(name = "title", nullable = false, length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;

    @Size(max = 100, message = "位置不能超过100个字符")
    @Column(name = "location", length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String location;

    @Size(max = 50, message = "责任人不能超过50个字符")
    @Column(name = "responsible_person", length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String responsiblePerson;

    @Size(max = 500, message = "模板内容不能超过500个字符")
    @Column(name = "content", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String content;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "last_generated_at")
    private LocalDate lastGeneratedAt;

    @Column(name = "next_generate_date", nullable = false)
    private LocalDate nextGenerateDate;

    public enum Frequency {
        DAILY("每天"),
        WEEKLY("每周"),
        BIWEEKLY("每两周"),
        MONTHLY("每月");

        private final String description;

        Frequency(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
