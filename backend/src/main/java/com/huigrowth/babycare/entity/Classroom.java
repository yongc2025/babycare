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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 托育班级实体
 */
@Entity
@Table(name = "classrooms", indexes = {
    @Index(name = "idx_classroom_organization", columnList = "organization_id"),
    @Index(name = "idx_classroom_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"organization"})
@ToString(exclude = {"organization"})
public class Classroom extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @NotBlank(message = "班级名称不能为空")
    @Size(min = 2, max = 50, message = "班级名称长度必须在2-50个字符之间")
    @Column(name = "name", nullable = false, length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String name;

    @Min(value = 0, message = "最小月龄不能小于0")
    @Column(name = "age_range_min_months")
    private Integer ageRangeMinMonths;

    @Min(value = 0, message = "最大月龄不能小于0")
    @Column(name = "age_range_max_months")
    private Integer ageRangeMaxMonths;

    @Min(value = 0, message = "托位容量不能小于0")
    @Column(name = "capacity")
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ClassroomStatus status = ClassroomStatus.ACTIVE;

    public enum ClassroomStatus {
        ACTIVE("启用"),
        DISABLED("停用");

        private final String description;

        ClassroomStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
