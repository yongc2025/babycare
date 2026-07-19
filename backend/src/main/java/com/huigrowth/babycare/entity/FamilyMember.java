package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * 家庭成员实体
 * 
 * @author HuiGrowth Team
 */
@Entity
@Table(name = "family_members", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "family_id"}),
       indexes = {
           @Index(name = "idx_family_member_user", columnList = "user_id"),
           @Index(name = "idx_family_member_family", columnList = "family_id")
       })
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(exclude = {"user", "family"})
public class FamilyMember extends BaseEntity {

    @NotNull(message = "用户不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull(message = "家庭不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private FamilyRole role;

    @Column(name = "nickname", length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String nickname;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    // 长辈授权权限（T073）
    @Column(name = "can_confirm_pickup", nullable = false)
    private Boolean canConfirmPickup = false;

    @Column(name = "can_confirm_notification", nullable = false)
    private Boolean canConfirmNotification = false;

    /**
     * 家庭角色枚举
     */
    public enum FamilyRole {
        CREATOR("创建者"),
        PARENT("父母"),
        GRANDPARENT("祖父母/外祖父母"),
        RELATIVE("其他亲属");

        private final String description;

        FamilyRole(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}