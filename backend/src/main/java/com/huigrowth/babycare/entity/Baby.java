package com.huigrowth.babycare.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;

/**
 * 宝宝实体
 * 
 * @author HuiGrowth Team
 */
@Entity
@Table(name = "babies", indexes = {
    @Index(name = "idx_baby_family", columnList = "family_id"),
    @Index(name = "idx_baby_birthday", columnList = "birthday")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"family", "growthRecords", "milestones"})
@ToString(exclude = {"family", "growthRecords", "milestones"})
public class Baby extends BaseEntity {

    @NotBlank(message = "宝宝姓名不能为空")
    @Size(min = 1, max = 20, message = "宝宝姓名长度必须在1-20个字符之间")
    @Column(name = "name", nullable = false, length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String name;

    @NotNull(message = "性别不能为空")
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @NotNull(message = "生日不能为空")
    @Past(message = "生日必须是过去的日期")
    @Column(name = "birthday", nullable = false)
    private LocalDate birthday;

    @Column(name = "avatar", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String avatar;

    @NotNull(message = "家庭不能为空")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_id", nullable = false)
    private Family family;

    @Column(name = "birth_weight")
    private Double birthWeight; // 出生体重（克）

    @Column(name = "birth_height")
    private Double birthHeight; // 出生身长（厘米）

    @Column(name = "current_weight")
    private Double currentWeight; // 当前体重（克）

    @Column(name = "current_height")
    private Double currentHeight; // 当前身高（厘米）

    @Column(name = "description", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String description; // 宝宝简介

    @Size(max = 18, message = "身份证号不能超过18个字符")
    @Column(name = "id_card", length = 18, columnDefinition = "VARCHAR(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String idCard; // 宝宝身份证号

    @Size(max = 30, message = "出生证编号不能超过30个字符")
    @Column(name = "birth_certificate_no", length = 30, columnDefinition = "VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String birthCertificateNo; // 出生证编号

    @OneToMany(mappedBy = "baby", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<GrowthRecord> growthRecords;

    @OneToMany(mappedBy = "baby", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Milestone> milestones;

    /**
     * 性别枚举
     */
    public enum Gender {
        MALE("男"),
        FEMALE("女");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}