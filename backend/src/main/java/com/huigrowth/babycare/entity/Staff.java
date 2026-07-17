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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 机构员工实体
 */
@Entity
@Table(name = "staff", indexes = {
    @Index(name = "idx_staff_organization", columnList = "organization_id"),
    @Index(name = "idx_staff_user", columnList = "user_id"),
    @Index(name = "idx_staff_role", columnList = "role"),
    @Index(name = "idx_staff_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_staff_organization_user", columnNames = {"organization_id", "user_id"})
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"organization", "user"})
@ToString(exclude = {"organization", "user"})
public class Staff extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private StaffRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StaffStatus status = StaffStatus.ACTIVE;

    public enum StaffRole {
        DIRECTOR("园长"),
        TEACHER("老师"),
        CAREGIVER("保育员"),
        FINANCE("财务");

        private final String description;

        StaffRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum StaffStatus {
        ACTIVE("在职"),
        DISABLED("停用");

        private final String description;

        StaffStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
