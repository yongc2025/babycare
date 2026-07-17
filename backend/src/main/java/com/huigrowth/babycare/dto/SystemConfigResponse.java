package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.SystemConfig;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SystemConfigResponse {
    private Long id;
    private String configKey;
    private String configValue;
    private String configName;
    private String configGroup;
    private String status;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SystemConfigResponse fromEntity(SystemConfig c) {
        SystemConfigResponse r = new SystemConfigResponse();
        r.setId(c.getId());
        r.setConfigKey(c.getConfigKey());
        r.setConfigValue(c.getConfigValue());
        r.setConfigName(c.getConfigName());
        r.setConfigGroup(c.getConfigGroup());
        r.setStatus(c.getStatus() != null ? c.getStatus().name() : null);
        r.setRemark(c.getRemark());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}
