package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.CareRecord;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CareRecordResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private LocalDate recordDate;
    private LocalDateTime recordTime;
    private CareRecord.CareType type;
    private String typeDescription;
    private String valueText;
    private Double amount;
    private String unit;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String remark;
    private String source;
    private Long recordedById;
    private String recordedByName;
    private Boolean isBackfill;
    private String backfillReason;
    private Long backfilledById;
    private String backfilledByName;
    private LocalDateTime backfilledAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
