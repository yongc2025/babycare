package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 员工-班级分配请求
 */
@Data
public class StaffClassroomAssignmentRequest {

    @NotNull(message = "员工ID不能为空")
    private Long staffId;

    @NotNull(message = "班级ID不能为空")
    private Long classroomId;

    private String assignmentType; // TEACHER / CAREGIVER / ASSISTANT
}
