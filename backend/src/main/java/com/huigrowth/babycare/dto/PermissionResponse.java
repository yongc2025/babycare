package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.Permission;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PermissionResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String resourceType;
    private String method;
    private String urlPattern;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PermissionResponse fromEntity(Permission p) {
        PermissionResponse r = new PermissionResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setCode(p.getCode());
        r.setDescription(p.getDescription());
        r.setResourceType(p.getResourceType() != null ? p.getResourceType().name() : null);
        r.setMethod(p.getMethod());
        r.setUrlPattern(p.getUrlPattern());
        r.setStatus(p.getStatus() != null ? p.getStatus().name() : null);
        r.setCreatedAt(p.getCreatedAt());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
