package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 入托档案家长/监护人授权关系
 * <p>
 * 将 User（家长账号）与 Enrollment（入托档案）建立正式授权绑定。
 * 支持机构直接绑定、邀请码绑定、自助注册绑定三种方式。
 */
@Entity
@Table(name = "enrollment_guardians", uniqueConstraints = {
    @UniqueConstraint(name = "uk_guardian_enrollment_user", columnNames = {"enrollment_id", "user_id"})
}, indexes = {
    @Index(name = "idx_eg_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_eg_user", columnList = "user_id")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "guardianUser"})
@ToString(exclude = {"enrollment", "guardianUser"})
public class EnrollmentGuardian extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User guardianUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "relationship", nullable = false, length = 20)
    private GuardianRelationship relationship = GuardianRelationship.OTHER;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary = false;

    @Size(max = 18, message = "身份证号不能超过18个字符")
    @Column(name = "id_card", length = 18, columnDefinition = "VARCHAR(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String idCard; // 监护人身份证号

    @Size(max = 50, message = "监护人职业不能超过50个字符")
    @Column(name = "occupation", length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String occupation; // 监护人职业

    @Size(max = 20, message = "监护人电话不能超过20个字符")
    @Column(name = "guardian_phone", length = 20)
    private String guardianPhone;

    @Size(max = 200, message = "备注不能超过200个字符")
    @Column(name = "remark", length = 200, columnDefinition = "VARCHAR(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    @Enumerated(EnumType.STRING)
    @Column(name = "bind_type", nullable = false, length = 20)
    private BindType bindType = BindType.DIRECT_BIND;

    @Size(max = 32, message = "邀请码不能超过32个字符")
    @Column(name = "invite_code", length = 32)
    private String inviteCode;

    /**
     * 监护人与宝宝关系
     */
    public enum GuardianRelationship {
        FATHER("父亲"),
        MOTHER("母亲"),
        GRANDFATHER("祖父/外祖父"),
        GRANDMOTHER("祖母/外祖母"),
        OTHER("其他亲属");

        private final String description;

        GuardianRelationship(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 绑定方式
     */
    public enum BindType {
        DIRECT_BIND("机构直接绑定"),
        INVITE_CODE("邀请码绑定"),
        SELF_REGISTER("自助注册绑定");

        private final String description;

        BindType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
