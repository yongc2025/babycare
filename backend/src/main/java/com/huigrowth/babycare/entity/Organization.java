package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 托育机构实体
 * 支持集团-连锁-单体的多级组织架构。
 */
@Entity
@Table(name = "organizations", indexes = {
    @Index(name = "idx_organization_created_by", columnList = "created_by"),
    @Index(name = "idx_organization_status", columnList = "status"),
    @Index(name = "idx_organization_org_group", columnList = "org_group_id"),
    @Index(name = "idx_organization_parent", columnList = "parent_id")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"createdBy"})
@ToString(exclude = {"createdBy"})
public class Organization extends BaseEntity {

    @NotBlank(message = "机构名称不能为空")
    @Size(min = 2, max = 80, message = "机构名称长度必须在2-80个字符之间")
    @Column(name = "name", nullable = false, length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String name;

    @Size(max = 300, message = "机构简介长度不能超过300个字符")
    @Column(name = "description", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String description;

    @Column(name = "contact_phone", length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String contactPhone;

    @Column(name = "address", length = 200, columnDefinition = "VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String address;

    @Column(name = "registration_no", length = 60, columnDefinition = "VARCHAR(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String registrationNo;

    @Column(name = "license_no", length = 60, columnDefinition = "VARCHAR(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String licenseNo;

    @Column(name = "legal_representative", length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String legalRepresentative;

    @Column(name = "supervisor_department", length = 100, columnDefinition = "VARCHAR(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String supervisorDepartment;

    @Column(name = "organization_level", length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String organizationLevel;

    @Column(name = "operation_type", length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String operationType;

    /** 所属集团品牌ID（关联 org_group 表） */
    @Column(name = "org_group_id")
    private Long orgGroupId;

    /** 上级机构ID，支持连锁-分园层级 */
    @Column(name = "parent_id")
    private Long parentId;

    /** 机构类型：SINGLE-单体机构 HEADQUARTERS-总部/总园 BRANCH-分园/分校 */
    @Enumerated(EnumType.STRING)
    @Column(name = "org_type", length = 20)
    private OrgType orgType = OrgType.SINGLE;

    /** 日报是否需要园长审核后才能发布 */
    @Column(name = "is_daily_report_approval_required")
    private Boolean dailyReportApprovalRequired = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public enum OrgType {
        SINGLE("单体机构"),
        HEADQUARTERS("总部/总园"),
        BRANCH("分园/分校");

        private final String description;
        OrgType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    public enum OrganizationStatus {
        ACTIVE("启用"),
        DISABLED("停用");

        private final String description;
        OrganizationStatus(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
}
