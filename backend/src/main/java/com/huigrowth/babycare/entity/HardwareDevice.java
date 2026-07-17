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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "hardware_devices", indexes = {
    @Index(name = "idx_hardware_device_organization", columnList = "organization_id"),
    @Index(name = "idx_hardware_device_classroom", columnList = "classroom_id"),
    @Index(name = "idx_hardware_device_type", columnList = "device_type"),
    @Index(name = "idx_hardware_device_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_hardware_device_org_code", columnNames = {"organization_id", "device_code"})
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"organization", "classroom"})
@ToString(exclude = {"organization", "classroom"})
public class HardwareDevice extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @NotBlank(message = "设备编码不能为空")
    @Size(max = 80, message = "设备编码长度不能超过80个字符")
    @Column(name = "device_code", nullable = false, length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String deviceCode;

    @NotBlank(message = "设备名称不能为空")
    @Size(max = 80, message = "设备名称长度不能超过80个字符")
    @Column(name = "name", nullable = false, length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", nullable = false, length = 40)
    private DeviceType deviceType = DeviceType.OTHER;

    @Size(max = 80, message = "厂商长度不能超过80个字符")
    @Column(name = "vendor", length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String vendor;

    @Size(max = 80, message = "型号长度不能超过80个字符")
    @Column(name = "model", length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String model;

    @Size(max = 120, message = "位置长度不能超过120个字符")
    @Column(name = "location", length = 120, columnDefinition = "VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String location;

    @Size(max = 40, message = "接入模式长度不能超过40个字符")
    @Column(name = "integration_mode", length = 40, columnDefinition = "VARCHAR(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String integrationMode = "MANUAL_GATEWAY";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private DeviceStatus status = DeviceStatus.ACTIVE;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Size(max = 300, message = "备注长度不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    public enum DeviceType {
        ATTENDANCE_TERMINAL("考勤机"),
        HEALTH_CHECK_ROBOT("晨检机器人"),
        CAMERA("摄像头"),
        ACCESS_CONTROL("门禁"),
        SMART_CLASS_CARD("电子班牌"),
        OTHER("其他");

        private final String description;

        DeviceType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum DeviceStatus {
        ACTIVE("启用"),
        DISABLED("停用"),
        MAINTENANCE("维护中");

        private final String description;

        DeviceStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
