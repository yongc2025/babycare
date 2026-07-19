package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 班级-员工关联（org_classroom_staff）
 * 支持教师/保育员分配到指定班级。
 */
@Entity
@Table(name = "org_classroom_staff", indexes = {
    @Index(name = "idx_cs_classroom", columnList = "classroom_id"),
    @Index(name = "idx_cs_staff", columnList = "staff_id")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_cs_classroom_staff", columnNames = {"classroom_id", "staff_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class ClassroomStaff extends BaseEntity {

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "role_in_class", length = 20)
    @Enumerated(EnumType.STRING)
    private ClassRole roleInClass = ClassRole.TEACHER;

    @Column(name = "is_lead", nullable = false)
    private Boolean isLead = false;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private AssignStatus status = AssignStatus.ACTIVE;

    public enum ClassRole {
        TEACHER("教师"),
        CAREGIVER("保育员"),
        ASSISTANT("助教");

        private final String description;
        ClassRole(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public enum AssignStatus {
        ACTIVE("在职"),
        INACTIVE("离岗");

        private final String description;
        AssignStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
