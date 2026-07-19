package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author HuiGrowth Team
 */
@Tag(name = "系统监控", description = "系统健康检查和状态监控接口")
@RestController
@RequestMapping("/public")
public class HealthController {

    @Operation(summary = "健康检查", description = "检查系统运行状态")
    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", LocalDateTime.now());
        status.put("service", "BabyCare Backend");
        status.put("version", "1.0.0");
        return ApiResponse.success("系统运行正常", status);
    }

    @Operation(summary = "系统信息", description = "获取系统基本信息")
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", "好芽儿育儿平台");
        info.put("description", "智能化全家庭教育育儿平台");
        info.put("version", "1.0.0");
        info.put("author", "HuiGrowth Team");
        return ApiResponse.success(info);
    }
}