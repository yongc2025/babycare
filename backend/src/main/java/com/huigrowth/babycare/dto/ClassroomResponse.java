package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.Classroom;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 托育班级响应
 */
@Data
public class ClassroomResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private String name;
    private Integer ageRangeMinMonths;
    private Integer ageRangeMaxMonths;
    private Integer capacity;
    private Classroom.ClassroomStatus status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
