package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.HardwareDevice;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HardwareDeviceResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private Long classroomId;
    private String classroomName;
    private String deviceCode;
    private String name;
    private HardwareDevice.DeviceType deviceType;
    private String deviceTypeDescription;
    private String vendor;
    private String model;
    private String location;
    private String integrationMode;
    private HardwareDevice.DeviceStatus status;
    private String statusDescription;
    private LocalDateTime lastSeenAt;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
