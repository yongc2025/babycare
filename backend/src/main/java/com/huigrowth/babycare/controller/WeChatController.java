package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 微信登录预留控制器（T080）
 * <p>
 * 当前为占位实现，返回"待接入"提示。
 * 正式接入时需对接微信小程序登录和手机号授权。
 */
@Slf4j
@Tag(name = "微信登录", description = "微信小程序登录和手机号授权预留接口")
@RestController
@RequestMapping("/wechat")
@RequiredArgsConstructor
public class WeChatController {

    @Operation(summary = "微信小程序登录", description = "使用微信临时 code 登录（预留占位）")
    @PostMapping("/mini-program/login")
    public ApiResponse<WeChatLoginResponse> miniProgramLogin(
            @RequestBody WeChatLoginRequest request) {
        log.info("微信登录请求 code={}，当前为占位实现", request.getCode());
        WeChatLoginResponse resp = new WeChatLoginResponse();
        resp.setSuccess(false);
        resp.setMessage("微信登录功能待接入");
        return ApiResponse.success(resp);
    }

    @Operation(summary = "微信手机号授权", description = "获取微信用户手机号并绑定（预留占位）")
    @PostMapping("/mini-program/phone")
    public ApiResponse<WeChatPhoneResponse> miniProgramPhone(
            @RequestBody WeChatPhoneRequest request) {
        log.info("微信手机号授权请求，当前为占位实现");
        WeChatPhoneResponse resp = new WeChatPhoneResponse();
        resp.setSuccess(false);
        resp.setMessage("微信手机号授权功能待接入");
        return ApiResponse.success(resp);
    }

    // ========== 内部 DTO ==========

    @lombok.Data
    public static class WeChatLoginRequest {
        private String code;
    }

    @lombok.Data
    public static class WeChatLoginResponse {
        private boolean success;
        private String token;
        private String message;
    }

    @lombok.Data
    public static class WeChatPhoneRequest {
        private String code;
        private String encryptedData;
        private String iv;
    }

    @lombok.Data
    public static class WeChatPhoneResponse {
        private boolean success;
        private String phone;
        private String message;
    }
}
