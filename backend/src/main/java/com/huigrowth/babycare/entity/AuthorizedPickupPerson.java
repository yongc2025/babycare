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

@Entity
@Table(name = "authorized_pickup_persons", indexes = {
    @Index(name = "idx_pickup_person_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_pickup_person_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "createdBy"})
@ToString(exclude = {"enrollment", "createdBy"})
public class AuthorizedPickupPerson extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Size(max = 30, message = "接送人姓名不能超过30个字符")
    @Column(name = "name", nullable = false, length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String name;

    @Size(max = 30, message = "接送关系不能超过30个字符")
    @Column(name = "relationship", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String relationship;

    @Size(max = 20, message = "接送人电话不能超过20个字符")
    @Column(name = "phone", length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String phone;

    @Size(max = 80, message = "证件号不能超过80个字符")
    @Column(name = "identity_no", length = 80, columnDefinition = "VARCHAR(80) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String identityNo;

    @Size(max = 500, message = "照片地址不能超过500个字符")
    @Column(name = "photo_url", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String photoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PickupPersonStatus status = PickupPersonStatus.ACTIVE;

    @Size(max = 300, message = "备注不能超过300个字符")
    @Column(name = "remark", length = 300, columnDefinition = "VARCHAR(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    public enum PickupPersonStatus {
        ACTIVE("启用"),
        DISABLED("停用");

        private final String description;

        PickupPersonStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
