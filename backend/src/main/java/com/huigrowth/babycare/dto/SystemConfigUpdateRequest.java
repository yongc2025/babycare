package com.huigrowth.babycare.dto;

import lombok.Data;

@Data
public class SystemConfigUpdateRequest {
    private String configValue;
    private String configName;
    private String configGroup;
    private String status;
    private String remark;
}
