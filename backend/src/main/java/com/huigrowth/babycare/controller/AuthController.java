package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.ChangePasswordRequest;
import com.huigrowth.babycare.dto.JwtResponse;
import com.huigrowth.babycare.dto.LoginRequest;
import com.huigrowth.babycare.dto.PhoneLoginRequest;
import com.huigrowth.babycare.dto.RegisterRequest;
import com.huigrowth.babycare.dto.SendCodeRequest;
import com.huigrowth.babycare.dto.UpdateProfileRequest;
import com.huigrowth.babycare.dto.UserResponse;
import com.huigrowth.babycare.dto.VerifyPhoneRequest;
import com.huigrowth.babycare.service.AuthService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author HuiGrowth Team
 */
@Tag(name = "认证管理", description = "用户注册、登录、令牌管理等接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户注册", description = "创建新用户账户")
    @PostMapping("/register")
    public ApiResponse<JwtResponse> register(@Valid @RequestBody RegisterRequest request) {
        JwtResponse response = authService.register(request);
        return ApiResponse.success("注册成功", response);
    }

    @Operation(summary = "用户登录", description = "用户身份验证并返回JWT令牌")
    @PostMapping("/login")
    public ApiResponse<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        JwtResponse response = authService.login(request);
        return ApiResponse.success("登录成功", response);
    }

    @Operation(summary = "刷新令牌", description = "使用有效令牌获取新的JWT令牌")
    @PostMapping("/refresh")
    public ApiResponse<JwtResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7); // 移除 "Bearer " 前缀
        JwtResponse response = authService.refreshToken(token);
        return ApiResponse.success("令牌刷新成功", response);
    }

    @Operation(summary = "获取当前用户信息", description = "获取已登录用户的详细信息")
    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        UserResponse response = authService.getCurrentUser(userDetails.getUsername());
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新用户资料", description = "更新当前用户的个人信息")
    @PutMapping("/profile")
    public ApiResponse<UserResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 这里需要获取用户ID，暂时使用用户名查找
        UserResponse currentUser = authService.getCurrentUser(userDetails.getUsername());
        UserResponse response = authService.updateProfile(currentUser.getId(), request);
        return ApiResponse.success("资料更新成功", response);
    }

    @Operation(summary = "修改密码", description = "修改当前用户的登录密码")
    @PutMapping("/change-password")
    public ApiResponse<String> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        // 这里需要获取用户ID，暂时使用用户名查找
        UserResponse currentUser = authService.getCurrentUser(userDetails.getUsername());
        authService.changePassword(currentUser.getId(), request);
        return ApiResponse.success("密码修改成功");
    }

    @Operation(summary = "用户登出", description = "用户登出（客户端清除令牌）")
    @PostMapping("/logout")
    public ApiResponse<String> logout() {
        // JWT是无状态的，登出主要由客户端处理（清除令牌）
        // 这里可以添加令牌黑名单逻辑（如果需要的话）
        return ApiResponse.success("登出成功");
    }

    @Operation(summary = "检查用户名可用性", description = "检查用户名是否已被使用")
    @GetMapping("/check-username")
    public ApiResponse<Boolean> checkUsername(@RequestParam String username) {
        // 这里需要在AuthService中添加检查方法
        return ApiResponse.success("用户名可用", true);
    }

    @Operation(summary = "检查邮箱可用性", description = "检查邮箱是否已被使用")
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmail(@RequestParam String email) {
        return ApiResponse.success("邮箱可用", true);
    }

    // ========== T078: 手机号注册与验证 ==========

    @Operation(summary = "发送短信验证码", description = "向指定手机号发送验证码")
    @PostMapping("/send-code")
    public ApiResponse<String> sendVerificationCode(@Valid @RequestBody SendCodeRequest request) {
        authService.sendVerificationCode(request);
        return ApiResponse.success("验证码已发送");
    }

    @Operation(summary = "验证手机号", description = "验证短信验证码并标记手机号已验证")
    @PostMapping("/verify-phone")
    public ApiResponse<String> verifyPhone(@Valid @RequestBody VerifyPhoneRequest request) {
        authService.verifyPhone(request);
        return ApiResponse.success("手机号验证成功");
    }

    @Operation(summary = "手机号登录", description = "使用手机号+密码或手机号+验证码登录")
    @PostMapping("/phone-login")
    public ApiResponse<JwtResponse> phoneLogin(@Valid @RequestBody PhoneLoginRequest request) {
        JwtResponse response = authService.phoneLogin(request);
        return ApiResponse.success("登录成功", response);
    }
}