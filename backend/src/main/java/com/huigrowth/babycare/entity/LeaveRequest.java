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
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 幼儿请假申请实体
 */
@Entity
@Table(name = "leave_requests", indexes = {
    @Index(name = "idx_leave_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_leave_status", columnList = "status"),
    @Index(name = "idx_leave_date_range", columnList = "start_date,end_date")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "requestedBy", "reviewedBy"})
@ToString(exclude = {"enrollment", "requestedBy", "reviewedBy"})
public class LeaveRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private LeaveType type = LeaveType.PERSONAL;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LeaveStatus status = LeaveStatus.PENDING;

    @Size(max = 300, message = "请假原因不能超过300个字符")
    @Column(name = "reason", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String reason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Size(max = 300, message = "审批备注不能超过300个字符")
    @Column(name = "review_remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String reviewRemark;

    public enum LeaveType {
        SICK("病假"),
        PERSONAL("事假"),
        OTHER("其他");

        private final String description;

        LeaveType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum LeaveStatus {
        PENDING("待审批"),
        APPROVED("已通过"),
        REJECTED("已拒绝"),
        CANCELLED("已取消");

        private final String description;

        LeaveStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
