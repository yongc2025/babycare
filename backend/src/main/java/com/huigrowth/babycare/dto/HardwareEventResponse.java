package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.HardwareEvent;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HardwareEventResponse {
    private Long id;
    private Long deviceId;
    private String deviceCode;
    private String deviceName;
    private Long organizationId;
    private String organizationName;
    private Long classroomId;
    private String classroomName;
    private Long enrollmentId;
    private String babyName;
    private HardwareEvent.EventType eventType;
    private String eventTypeDescription;
    private LocalDateTime eventTime;
    private String eventKey;
    private String subjectRef;
    private Double confidence;
    private String payload;
    private HardwareEvent.EventStatus status;
    private String statusDescription;
    private LocalDateTime processedAt;
    private String processRemark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
