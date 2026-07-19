package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.EnrollmentGuardian.GuardianRelationship;
import com.huigrowth.babycare.entity.EnrollmentGuardian.BindType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入托监护人响应
 */
@Data
public class EnrollmentGuardianResponse {

    private Long id;
    private Long enrollmentId;
    private Long userId;
    private String userName;
    private String userNickname;
    private String userPhone;
    private String relationship;
    private String relationshipDescription;
    private Boolean isPrimary;
    private String idCard;
    private String occupation;
    private String guardianPhone;
    private String remark;
    private String bindType;
    private String bindTypeDescription;
    private LocalDateTime createdAt;
}
