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

import java.time.LocalDateTime;

@Entity
@Table(name = "announcements", indexes = {
    @Index(name = "idx_announcement_org", columnList = "organization_id"),
    @Index(name = "idx_announcement_classroom", columnList = "classroom_id"),
    @Index(name = "idx_announcement_status", columnList = "status")
})
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"organization", "classroom", "publishedBy"})
@ToString(exclude = {"organization", "classroom", "publishedBy"})
public class Announcement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "classroom_id")
    private Classroom classroom;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope", nullable = false, length = 20)
    private AnnouncementScope scope = AnnouncementScope.ORGANIZATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AnnouncementStatus status = AnnouncementStatus.DRAFT;

    @Size(max = 120, message = "通知标题不能超过120个字符")
    @Column(name = "title", nullable = false, length = 120, columnDefinition = "VARCHAR(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String title;

    @Size(max = 3000, message = "通知内容不能超过3000个字符")
    @Column(name = "content", nullable = false, length = 3000, columnDefinition = "VARCHAR(3000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci")
    private String content;

    @Column(name = "require_receipt", nullable = false)
    private Boolean requireReceipt = true;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "published_by")
    private User publishedBy;

    public enum AnnouncementScope {
        ORGANIZATION("机构"),
        CLASSROOM("班级");

        private final String description;

        AnnouncementScope(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum AnnouncementStatus {
        DRAFT("草稿"),
        PUBLISHED("已发布");

        private final String description;

        AnnouncementStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
