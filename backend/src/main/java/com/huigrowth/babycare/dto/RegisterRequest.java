package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册请求DTO
 * <p>
 * T078 增强：支持手机号为主注册，用户名和邮箱均为可选。
 * 如果只传 phone，系统自动生成用户名 (u_{phone})。
 */
@Data
public class RegisterRequest {

    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @Email(message = "邮箱格式不正确")
    private String email;

    @Size(min = 6, message = "密码长度至少6位")
    private String password;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    @Size(max = 10, message = "昵称不能超过10个字符")
    private String nickname;

    @Size(max = 50, message = "城市不能超过50个字符")
    private String city;

    /** 注册时是否已验证手机号（前端通过验证码验证后传入） */
    private boolean phoneVerified = false;
}
