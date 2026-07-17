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
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_reports", indexes = {
    @Index(name = "idx_daily_report_enrollment", columnList = "enrollment_id"),
    @Index(name = "idx_daily_report_date", columnList = "report_date"),
    @Index(name = "idx_daily_report_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_daily_report_enrollment_date", columnNames = {"enrollment_id", "report_date"})
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"enrollment", "publishedBy"})
@ToString(exclude = {"enrollment", "publishedBy"})
public class DailyReport extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ReportStatus status = ReportStatus.DRAFT;

    @Size(max = 500, message = "日报摘要不能超过500个字符")
    @Column(name = "summary", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String summary;

    @Size(max = 500, message = "考勤摘要不能超过500个字符")
    @Column(name = "attendance_summary", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String attendanceSummary;

    @Size(max = 1000, message = "照护摘要不能超过1000个字符")
    @Column(name = "care_summary", length = 1000, columnDefinition = "VARCHAR(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String careSummary;

    @Size(max = 500, message = "健康摘要不能超过500个字符")
    @Column(name = "health_summary", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String healthSummary;

    @Size(max = 500, message = "活动摘要不能超过500个字符")
    @Column(name = "activity_summary", length = 500, columnDefinition = "VARCHAR(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String activitySummary;

    @Size(max = 800, message = "老师评语不能超过800个字符")
    @Column(name = "teacher_comment", length = 800, columnDefinition = "VARCHAR(800) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String teacherComment;

    @Size(max = 1200, message = "AI草稿不能超过1200个字符")
    @Column(name = "ai_draft_content", length = 1200, columnDefinition = "VARCHAR(1200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String aiDraftContent;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    public enum ReportStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布");

        private final String description;

        ReportStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
