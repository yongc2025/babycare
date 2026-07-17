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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "admission_leads", indexes = {
    @Index(name = "idx_admission_lead_organization", columnList = "organization_id"),
    @Index(name = "idx_admission_lead_status", columnList = "status"),
    @Index(name = "idx_admission_lead_guardian_phone", columnList = "guardian_phone")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"organization", "intendedClassroom", "reviewedBy"})
@ToString(exclude = {"organization", "intendedClassroom", "reviewedBy"})
public class AdmissionLead extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "intended_classroom_id")
    private Classroom intendedClassroom;

    @NotBlank(message = "宝宝姓名不能为空")
    @Size(max = 50, message = "宝宝姓名不能超过50个字符")
    @Column(name = "child_name", nullable = false, length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String childName;

    @Column(name = "child_gender", length = 20)
    private String childGender;

    @Column(name = "child_birthday")
    private LocalDate childBirthday;

    @NotBlank(message = "家长姓名不能为空")
    @Size(max = 50, message = "家长姓名不能超过50个字符")
    @Column(name = "guardian_name", nullable = false, length = 50, columnDefinition = "VARCHAR(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String guardianName;

    @NotBlank(message = "家长手机号不能为空")
    @Size(max = 20, message = "家长手机号不能超过20个字符")
    @Column(name = "guardian_phone", nullable = false, length = 20, columnDefinition = "VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String guardianPhone;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 30)
    private LeadSource source = LeadSource.OTHER;

    @Enumerated(EnumType.STRING)
    @Column(name = "intention_level", nullable = false, length = 30)
    private IntentionLevel intentionLevel = IntentionLevel.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private LeadStatus status = LeadStatus.NEW;

    @Column(name = "preferred_start_date")
    private LocalDate preferredStartDate;

    @Column(name = "remark", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String remark;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "review_remark", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String reviewRemark;

    @Column(name = "trial_start_date")
    private LocalDate trialStartDate;

    @Column(name = "trial_end_date")
    private LocalDate trialEndDate;

    @Column(name = "trial_feedback", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String trialFeedback;

    public enum LeadSource {
        ONLINE("线上咨询"),
        REFERRAL("转介绍"),
        OPEN_DAY("开放日"),
        WALK_IN("到访"),
        OTHER("其他");

        private final String description;

        LeadSource(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum IntentionLevel {
        HIGH("高意向"),
        MEDIUM("待跟进"),
        LOW("低意向"),
        LOST("已流失");

        private final String description;

        IntentionLevel(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum LeadStatus {
        NEW("新线索"),
        FOLLOWING("跟进中"),
        APPLIED("已报名"),
        APPROVED("审核通过"),
        REJECTED("审核拒绝"),
        TRIALING("试托中"),
        TRIAL_COMPLETED("试托完成"),
        ENROLLED("已入托"),
        LOST("已流失");

        private final String description;

        LeadStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
