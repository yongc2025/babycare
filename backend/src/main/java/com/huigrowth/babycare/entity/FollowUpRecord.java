package com.huigrowth.babycare.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 招生线索跟进记录
 */
@Entity
@Table(name = "follow_up_records", indexes = {
    @Index(name = "idx_fur_lead", columnList = "admission_lead_id"),
    @Index(name = "idx_fur_next", columnList = "next_follow_up_at")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"admissionLead", "handledBy"})
@ToString(exclude = {"admissionLead", "handledBy"})
public class FollowUpRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admission_lead_id", nullable = false)
    private AdmissionLead admissionLead;

    @Size(max = 1000, message = "跟进内容不能超过1000个字符")
    @Column(name = "content", length = 1000, columnDefinition = "VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handled_by")
    private User handledBy;

    @Column(name = "next_follow_up_at")
    private LocalDateTime nextFollowUpAt;
}
