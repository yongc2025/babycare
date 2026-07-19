package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 数据字典（sys_data_dict）
 * 管理系统级枚举和常量配置，如证件类型、民族、缴费方式等。
 */
@Entity
@Table(name = "sys_data_dict", indexes = {
    @Index(name = "idx_sys_dict_type", columnList = "dict_type"),
    @Index(name = "idx_sys_dict_type_code", columnList = "dict_type, item_code", unique = true)
})
@Data
@EqualsAndHashCode(callSuper = true)
public class DataDict extends BaseEntity {

    @Column(name = "dict_type", nullable = false, length = 100)
    private String dictType;

    @Column(name = "dict_name", nullable = false, length = 100)
    private String dictName;

    @Column(name = "item_code", nullable = false, length = 100)
    private String itemCode;

    @Column(name = "item_value", nullable = false, length = 255)
    private String itemValue;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private DictStatus status = DictStatus.ACTIVE;

    @Column(name = "remark", length = 255)
    private String remark;

    public enum DictStatus {
        ACTIVE("启用"),
        DISABLED("禁用");

        private final String description;
        DictStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
