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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 托育机构实体
 */
@Entity
@Table(name = "organizations", indexes = {
    @Index(name = "idx_organization_created_by", columnList = "created_by"),
    @Index(name = "idx_organization_status", columnList = "status")
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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrganizationStatus status = OrganizationStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    public enum OrganizationStatus {
        ACTIVE("启用"),
        DISABLED("停用");

        private final String description;

        OrganizationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
