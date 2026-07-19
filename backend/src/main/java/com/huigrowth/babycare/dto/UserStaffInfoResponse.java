package com.huigrowth.babycare.dto;

import lombok.Data;

import java.util.List;

/**
 * 当前用户的岗位与班级授权信息
 */
@Data
public class UserStaffInfoResponse {
    private List<StaffInfo> staffInfos;

    @Data
    public static class StaffInfo {
        private Long staffId;
        private Long organizationId;
        private String organizationName;
        private String role;
        private String roleDescription;
        private List<ClassroomInfo> assignedClassrooms;
    }

    @Data
    public static class ClassroomInfo {
        private Long classroomId;
        private String classroomName;
        private String assignmentType;
        private String assignmentTypeDescription;
    }
}
