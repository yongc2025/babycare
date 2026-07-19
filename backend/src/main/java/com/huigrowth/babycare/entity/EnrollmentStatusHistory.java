package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 入托档案状态变更历史（T077）
 * 记录转班、暂停、复托、退托等状态变更
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "enrollment_status_history", indexes = {
    @Index(name = "idx_esh_enrollment", columnList = "enrollment_id")
})
public class EnrollmentStatusHistory extends BaseEntity {

    @Column(name = "enrollment_id", nullable = false)
    private Long enrollmentId;

    @Column(name = "from_status", length = 30)
    private String fromStatus;

    @Column(name = "to_status", length = 30, nullable = false)
    private String toStatus;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 50)
    private String operatorName;

    @Size(max = 300, message = "备注不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;
}
