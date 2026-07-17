package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 系统配置（sys_config）
 * 存储键值对形式的系统级配置项。
 */
@Entity
@Table(name = "sys_config", indexes = {
    @Index(name = "idx_sys_config_key", columnList = "config_key", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true)
public class SystemConfig extends BaseEntity {

    @Column(name = "config_key", nullable = false, length = 100, unique = true)
    private String configKey;

    @Column(name = "config_value", columnDefinition = "TEXT")
    private String configValue;

    @Column(name = "config_name", length = 100)
    private String configName;

    @Column(name = "config_group", length = 50)
    private String configGroup;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ConfigStatus status = ConfigStatus.ACTIVE;

    @Column(name = "remark", length = 255)
    private String remark;

    public enum ConfigStatus {
        ACTIVE("启用"),
        DISABLED("禁用");

        private final String description;
        ConfigStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
