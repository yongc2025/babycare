package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.DailyReport;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class DailyReportResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private LocalDate reportDate;
    private DailyReport.ReportStatus status;
    private String statusDescription;
    private String summary;
    private String attendanceSummary;
    private String careSummary;
    private String healthSummary;
    private String activitySummary;
    private String teacherComment;
    private String aiDraftContent;
    private LocalDateTime publishedAt;
    private Long publishedById;
    private String publishedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
