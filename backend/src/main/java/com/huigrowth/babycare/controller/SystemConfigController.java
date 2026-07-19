package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.aspect.AuditLogAnnotation;
import com.huigrowth.babycare.dto.SystemConfigResponse;
import com.huigrowth.babycare.dto.SystemConfigUpdateRequest;
import com.huigrowth.babycare.service.SystemConfigService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统配置控制器
 */
@Tag(name = "系统配置", description = "系统配置管理接口")
@RestController
@RequestMapping("/admin/config")
@RequiredArgsConstructor
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    @Operation(summary = "获取所有配置")
    @GetMapping
    public ApiResponse<List<SystemConfigResponse>> listAll() {
        return ApiResponse.success(systemConfigService.listAll());
    }

    @Operation(summary = "根据键获取配置")
    @GetMapping("/{configKey}")
    public ApiResponse<SystemConfigResponse> getByKey(@PathVariable String configKey) {
        return ApiResponse.success(systemConfigService.getByKey(configKey));
    }

    @AuditLogAnnotation(action = "UPDATE_SYSTEM_CONFIG", actionName = "更新系统配置", targetType = "SystemConfig")
    @Operation(summary = "更新配置")
    @PutMapping("/{configKey}")
    public ApiResponse<SystemConfigResponse> update(@PathVariable String configKey,
                                                     @Valid @RequestBody SystemConfigUpdateRequest request) {
        return ApiResponse.success("更新成功", systemConfigService.update(configKey, request));
    }
}
