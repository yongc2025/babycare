package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.HealthObservation;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class HealthObservationResponse {
    private Long id;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long classroomId;
    private String classroomName;
    private Long organizationId;
    private String organizationName;
    private LocalDate observationDate;
    private LocalDateTime observationTime;
    private HealthObservation.ObservationType type;
    private String typeDescription;
    private Double temperature;
    private String touchStatus;
    private String lookStatus;
    private String askStatus;
    private String checkStatus;
    private String symptoms;
    private String actionTaken;
    private Boolean abnormal;
    private Boolean followUpRequired;
    private String source;
    private Long recordedById;
    private String recordedByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
