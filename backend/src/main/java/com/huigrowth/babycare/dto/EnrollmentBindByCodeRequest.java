package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 通过邀请码绑定入托档案请求
 */
@Data
public class EnrollmentBindByCodeRequest {

    @NotBlank(message = "邀请码不能为空")
    private String inviteCode;

    private String relationship = "OTHER";

    private Boolean isPrimary = false;

    private String guardianPhone;
}
