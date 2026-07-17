package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.AuditLogResponse;
import com.huigrowth.babycare.entity.AuditLog;
import com.huigrowth.babycare.service.AuditService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * 审计日志控制器
 */
@Tag(name = "审计日志", description = "审计日志查询接口")
@RestController
@RequestMapping("/api/admin/audit-log")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @Operation(summary = "查询审计日志")
    @GetMapping
    public ApiResponse<Page<AuditLogResponse>> queryLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<AuditLog> logs = auditService.queryLogs(action, targetType, username, startTime, endTime, page, size);
        Page<AuditLogResponse> resp = logs.map(AuditLogResponse::fromEntity);
        return ApiResponse.success(resp);
    }
}
