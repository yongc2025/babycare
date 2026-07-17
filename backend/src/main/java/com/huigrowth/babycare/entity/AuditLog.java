package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 审计日志（sys_audit_log）
 * 记录关键操作的创建人、时间、内容、IP等信息，用于追溯。
 */
@Entity
@Table(name = "sys_audit_log", indexes = {
    @Index(name = "idx_sys_audit_user", columnList = "user_id"),
    @Index(name = "idx_sys_audit_action", columnList = "action"),
    @Index(name = "idx_sys_audit_target", columnList = "target_type"),
    @Index(name = "idx_sys_audit_time", columnList = "created_at")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class AuditLog extends BaseEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "action_name", length = 100)
    private String actionName;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "result", length = 20)
    @Enumerated(EnumType.STRING)
    private AuditResult result = AuditResult.SUCCESS;

    public enum AuditResult {
        SUCCESS("成功"),
        FAILURE("失败"),
        DENIED("拒绝");

        private final String description;
        AuditResult(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
