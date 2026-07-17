package com.huigrowth.babycare.dto;

import lombok.Data;

/**
 * 更新机构员工请求
 */
@Data
public class StaffUpdateRequest {

    private String role;

    private String status;
}
