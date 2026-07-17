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
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 幼儿考勤记录实体
 */
@Entity
@Table(name = "attendance_records", indexes = {
    @Index(name = "idx_attendance_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_attendance_date", columnList = "attendance_date"),
    @Index(name = "idx_attendance_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_attendance_enrollment_date", columnNames = {"enrollment_id", "attendance_date"})
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "recordedBy"})
@ToString(exclude = {"enrollment", "recordedBy"})
public class AttendanceRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttendanceStatus status = AttendanceStatus.ABSENT;

    @Column(name = "check_in_at")
    private LocalDateTime checkInAt;

    @Column(name = "check_out_at")
    private LocalDateTime checkOutAt;

    @Column(name = "temperature")
    private Double temperature;

    @Size(max = 30, message = "接送人姓名不能超过30个字符")
    @Column(name = "pickup_person_name", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String pickupPersonName;

    @Size(max = 30, message = "接送关系不能超过30个字符")
    @Column(name = "pickup_relationship", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String pickupRelationship;

    @Size(max = 20, message = "接送人电话不能超过20个字符")
    @Column(name = "pickup_phone", length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String pickupPhone;

    @Size(max = 30, message = "数据来源不能超过30个字符")
    @Column(name = "source", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String source = "MANUAL";

    @Size(max = 300, message = "考勤备注不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by")
    private User recordedBy;

    public enum AttendanceStatus {
        ABSENT("缺勤"),
        CHECKED_IN("已到园"),
        CHECKED_OUT("已离园"),
        LEAVE("请假");

        private final String description;

        AttendanceStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
