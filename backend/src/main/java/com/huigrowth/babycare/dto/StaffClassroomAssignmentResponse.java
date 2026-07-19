package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.StaffClassroomAssignment;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 员工-班级分配响应
 */
@Data
public class StaffClassroomAssignmentResponse {
    private Long id;
    private Long staffId;
    private String staffName;
    private String staffNickname;
    private String staffRole;
    private Long classroomId;
    private String classroomName;
    private StaffClassroomAssignment.AssignmentType assignmentType;
    private String assignmentTypeDescription;
    private LocalDateTime createdAt;
}
