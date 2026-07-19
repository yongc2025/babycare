package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 集团/品牌（org_group）
 * 支持连锁品牌管理，一个集团可拥有多个托育机构。
 */
@Entity
@Table(name = "org_group", indexes = {
    @Index(name = "idx_org_group_code", columnList = "code", unique = true),
    @Index(name = "idx_org_group_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true)
public class OrgGroup extends BaseEntity {

    @Column(name = "name", nullable = false, length = 80)
    private String name;

    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Column(name = "description", length = 300)
    private String description;

    @Column(name = "logo", length = 500)
    private String logo;

    @Column(name = "contact_person", length = 50)
    private String contactPerson;

    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    @Column(name = "address", length = 200)
    private String address;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private GroupStatus status = GroupStatus.ACTIVE;

    public enum GroupStatus {
        ACTIVE("启用"),
        DISABLED("停用");

        private final String description;
        GroupStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
