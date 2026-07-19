package com.huigrowth.babycare.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FollowUpRecordResponse {
    private Long id;
    private Long admissionLeadId;
    private String content;
    private Long handledById;
    private String handledByName;
    private LocalDateTime nextFollowUpAt;
    private LocalDateTime createdAt;
}
