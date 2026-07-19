package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 员工-班级关联实体
 * 将教师/保育员分配到具体班级，支持按班级授权
 */
@Entity
@Table(name = "staff_classroom", indexes = {
    @Index(name = "idx_sca_staff", columnList = "staff_id"),
    @Index(name = "idx_sca_classroom", columnList = "classroom_id"),
    @Index(name = "idx_sca_type", columnList = "assignment_type")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_sca_staff_classroom", columnNames = {"staff_id", "classroom_id"})
})
@Data
@EqualsAndHashCode(callSuper = true)
public class StaffClassroomAssignment extends BaseEntity {

    @Column(name = "staff_id", nullable = false)
    private Long staffId;

    @Column(name = "classroom_id", nullable = false)
    private Long classroomId;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 20)
    private AssignmentType assignmentType = AssignmentType.TEACHER;

    public enum AssignmentType {
        TEACHER("教师"),
        CAREGIVER("保育员"),
        ASSISTANT("助教");

        private final String description;
        AssignmentType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
