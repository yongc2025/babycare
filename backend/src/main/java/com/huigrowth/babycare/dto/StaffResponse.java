package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.Staff;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 机构员工响应
 */
@Data
public class StaffResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private Long userId;
    private String username;
    private String nickname;
    private String phone;
    private String email;
    private Staff.StaffRole role;
    private String roleDescription;
    private Staff.StaffStatus status;
    private String statusDescription;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
