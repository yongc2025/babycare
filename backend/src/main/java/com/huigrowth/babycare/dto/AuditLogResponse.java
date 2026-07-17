package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.AuditLog;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String username;
    private String action;
    private String actionName;
    private String targetType;
    private Long targetId;
    private String details;
    private String ipAddress;
    private String result;
    private LocalDateTime createdAt;

    public static AuditLogResponse fromEntity(AuditLog log) {
        AuditLogResponse r = new AuditLogResponse();
        r.setId(log.getId());
        r.setUserId(log.getUserId());
        r.setUsername(log.getUsername());
        r.setAction(log.getAction());
        r.setActionName(log.getActionName());
        r.setTargetType(log.getTargetType());
        r.setTargetId(log.getTargetId());
        r.setDetails(log.getDetails());
        r.setIpAddress(log.getIpAddress());
        r.setResult(log.getResult() != null ? log.getResult().name() : null);
        r.setCreatedAt(log.getCreatedAt());
        return r;
    }
}
