package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 手机号登录请求（支持密码和验证码两种方式）
 */
@Data
public class PhoneLoginRequest {

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(min = 6, message = "密码至少6位")
    private String password;

    @Size(min = 4, max = 8, message = "验证码长度不正确")
    private String code;

    /**
     * 是否使用验证码登录
     */
    public boolean isCodeLogin() {
        return code != null && !code.isBlank();
    }
}
