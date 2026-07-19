package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.Organization;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 托育机构响应
 */
@Data
public class OrganizationResponse {
    private Long id;
    private String name;
    private String description;
    private String contactPhone;
    private String address;
    private String registrationNo;
    private String licenseNo;
    private String legalRepresentative;
    private String supervisorDepartment;
    private String organizationLevel;
    private String operationType;
    private Organization.OrganizationStatus status;
    private String statusDescription;
    private Long orgGroupId;
    private String orgGroupName;
    private Long parentId;
    private String parentName;
    private Organization.OrgType orgType;
    private String orgTypeDescription;
    private Boolean dailyReportApprovalRequired;
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
