package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.Announcement;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AnnouncementResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private Long classroomId;
    private String classroomName;
    private Announcement.AnnouncementScope scope;
    private String scopeDescription;
    private Announcement.AnnouncementStatus status;
    private String statusDescription;
    private String title;
    private String content;
    private Boolean requireReceipt;
    private LocalDateTime publishedAt;
    private Long publishedById;
    private String publishedByName;
    private Boolean readByCurrentUser;
    private LocalDateTime readAt;
    private Long receiptCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
