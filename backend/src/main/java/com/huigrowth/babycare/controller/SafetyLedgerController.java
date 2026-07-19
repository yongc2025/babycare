package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.SafetyLedgerHandleRequest;
import com.huigrowth.babycare.dto.SafetyLedgerOverdueResponse;
import com.huigrowth.babycare.dto.SafetyLedgerRequest;
import com.huigrowth.babycare.dto.SafetyLedgerResponse;
import com.huigrowth.babycare.dto.SafetyLedgerTemplateRequest;
import com.huigrowth.babycare.dto.SafetyLedgerTemplateResponse;
import com.huigrowth.babycare.service.SafetyLedgerService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "安全与卫生台账", description = "消毒、食品留样、设施巡检、消防和事故跟进台账接口")
@RestController
@RequestMapping("/safety-ledger")
@RequiredArgsConstructor
public class SafetyLedgerController {

    private final SafetyLedgerService safetyLedgerService;

    @Operation(summary = "创建安全卫生台账")
    @PostMapping("/create")
    public ApiResponse<SafetyLedgerResponse> createLedger(
            Authentication authentication,
            @Valid @RequestBody SafetyLedgerRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SafetyLedgerResponse response = safetyLedgerService.createLedger(userDetails.getUsername(), request);
        return ApiResponse.success("安全卫生台账创建成功", response);
    }

    @Operation(summary = "更新安全卫生台账")
    @PutMapping("/{ledgerId}")
    public ApiResponse<SafetyLedgerResponse> updateLedger(
            Authentication authentication,
            @PathVariable Long ledgerId,
            @Valid @RequestBody SafetyLedgerRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SafetyLedgerResponse response = safetyLedgerService.updateLedger(
                userDetails.getUsername(),
                ledgerId,
                request);
        return ApiResponse.success("安全卫生台账更新成功", response);
    }

    @Operation(summary = "标记台账处理中")
    @PostMapping("/{ledgerId}/processing")
    public ApiResponse<SafetyLedgerResponse> markProcessing(
            Authentication authentication,
            @PathVariable Long ledgerId,
            @RequestBody(required = false) SafetyLedgerHandleRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SafetyLedgerResponse response = safetyLedgerService.markProcessing(
                userDetails.getUsername(),
                ledgerId,
                request);
        return ApiResponse.success("安全卫生台账已标记处理中", response);
    }

    @Operation(summary = "关闭安全卫生台账")
    @PostMapping("/{ledgerId}/close")
    public ApiResponse<SafetyLedgerResponse> closeLedger(
            Authentication authentication,
            @PathVariable Long ledgerId,
            @RequestBody(required = false) SafetyLedgerHandleRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SafetyLedgerResponse response = safetyLedgerService.closeLedger(
                userDetails.getUsername(),
                ledgerId,
                request);
        return ApiResponse.success("安全卫生台账已关闭", response);
    }

    @Operation(summary = "机构安全卫生台账列表")
    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<SafetyLedgerResponse>> getOrganizationLedgers(
            Authentication authentication,
            @PathVariable Long organizationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<SafetyLedgerResponse> response = safetyLedgerService.getOrganizationLedgers(
                userDetails.getUsername(),
                organizationId,
                startDate,
                endDate,
                type,
                status);
        return ApiResponse.success(response);
    }

    @Operation(summary = "安全卫生台账详情")
    @GetMapping("/{ledgerId}")
    public ApiResponse<SafetyLedgerResponse> getLedgerDetail(
            Authentication authentication,
            @PathVariable Long ledgerId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SafetyLedgerResponse response = safetyLedgerService.getLedgerDetail(userDetails.getUsername(), ledgerId);
        return ApiResponse.success(response);
    }

    // ========== 台账模板 ==========

    @Operation(summary = "创建安全台账模板")
    @PostMapping("/template/create")
    public ApiResponse<SafetyLedgerTemplateResponse> createTemplate(
            Authentication authentication,
            @Valid @RequestBody SafetyLedgerTemplateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SafetyLedgerTemplateResponse response = safetyLedgerService.createTemplate(
                userDetails.getUsername(), request);
        return ApiResponse.success("安全台账模板创建成功", response);
    }

    @Operation(summary = "更新安全台账模板")
    @PutMapping("/template/{templateId}")
    public ApiResponse<SafetyLedgerTemplateResponse> updateTemplate(
            Authentication authentication,
            @PathVariable Long templateId,
            @Valid @RequestBody SafetyLedgerTemplateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SafetyLedgerTemplateResponse response = safetyLedgerService.updateTemplate(
                userDetails.getUsername(), templateId, request);
        return ApiResponse.success("安全台账模板更新成功", response);
    }

    @Operation(summary = "删除安全台账模板")
    @PostMapping("/template/{templateId}/delete")
    public ApiResponse<Void> deleteTemplate(
            Authentication authentication,
            @PathVariable Long templateId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        safetyLedgerService.deleteTemplate(userDetails.getUsername(), templateId);
        return ApiResponse.success("安全台账模板已删除", null);
    }

    @Operation(summary = "机构安全台账模板列表")
    @GetMapping("/template/organization/{organizationId}")
    public ApiResponse<List<SafetyLedgerTemplateResponse>> getOrganizationTemplates(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<SafetyLedgerTemplateResponse> response = safetyLedgerService.getOrganizationTemplates(
                userDetails.getUsername(), organizationId);
        return ApiResponse.success(response);
    }

    // ========== 周期任务生成与逾期检测 ==========

    @Operation(summary = "根据模板生成到期台账")
    @PostMapping("/generate-tasks/{organizationId}")
    public ApiResponse<String> generateTasks(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        int count = safetyLedgerService.generateTasks(userDetails.getUsername(), organizationId);
        return ApiResponse.success("成功生成" + count + "条台账任务");
    }

    @Operation(summary = "检查并标记逾期台账")
    @PostMapping("/check-overdue/{organizationId}")
    public ApiResponse<String> checkOverdue(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        int count = safetyLedgerService.checkOverdue(userDetails.getUsername(), organizationId);
        return ApiResponse.success("已标记" + count + "条逾期台账");
    }

    @Operation(summary = "机构台账统计（逾期/待处理/处理中数量）")
    @GetMapping("/overdue-count/{organizationId}")
    public ApiResponse<SafetyLedgerOverdueResponse> getOverdueCount(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        SafetyLedgerOverdueResponse response = safetyLedgerService.getOverdueCount(
                userDetails.getUsername(), organizationId);
        return ApiResponse.success(response);
    }
}
