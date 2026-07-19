package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.Role;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RoleResponse {
    private Long id;
    private String name;
    private String code;
    private String description;
    private String type;
    private Boolean system;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<Long> menuIds;
    private List<Long> permissionIds;
    private Integer userCount;

    public static RoleResponse fromEntity(Role role) {
        RoleResponse r = new RoleResponse();
        r.setId(role.getId());
        r.setName(role.getName());
        r.setCode(role.getCode());
        r.setDescription(role.getDescription());
        r.setType(role.getType() != null ? role.getType().name() : null);
        r.setSystem(role.getIsSystem());
        r.setStatus(role.getStatus() != null ? role.getStatus().name() : null);
        r.setCreatedAt(role.getCreatedAt());
        r.setUpdatedAt(role.getUpdatedAt());
        return r;
    }
}
