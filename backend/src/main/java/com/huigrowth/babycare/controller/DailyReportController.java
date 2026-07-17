package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.DailyReportGenerateRequest;
import com.huigrowth.babycare.dto.DailyReportResponse;
import com.huigrowth.babycare.dto.DailyReportUpdateRequest;
import com.huigrowth.babycare.service.DailyReportService;
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

@Tag(name = "结构化家长日报", description = "生成、编辑、发布和查询宝宝日报")
@RestController
@RequestMapping("/daily-report")
@RequiredArgsConstructor
public class DailyReportController {

    private final DailyReportService dailyReportService;

    @Operation(summary = "生成日报草稿")
    @PostMapping("/generate")
    public ApiResponse<DailyReportResponse> generateReport(
            Authentication authentication,
            @Valid @RequestBody DailyReportGenerateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        DailyReportResponse response = dailyReportService.generateReport(userDetails.getUsername(), request);
        return ApiResponse.success("日报草稿生成成功", response);
    }

    @Operation(summary = "生成 AI 日报辅助草稿")
    @PostMapping("/ai-draft/generate")
    public ApiResponse<DailyReportResponse> generateAiDraft(
            Authentication authentication,
            @Valid @RequestBody DailyReportGenerateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        DailyReportResponse response = dailyReportService.generateAiDraft(userDetails.getUsername(), request);
        return ApiResponse.success("AI 日报辅助草稿生成成功", response);
    }

    @Operation(summary = "更新日报草稿")
    @PutMapping("/{reportId}")
    public ApiResponse<DailyReportResponse> updateReport(
            Authentication authentication,
            @PathVariable Long reportId,
            @Valid @RequestBody DailyReportUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        DailyReportResponse response = dailyReportService.updateReport(
                userDetails.getUsername(),
                reportId,
                request);
        return ApiResponse.success("日报更新成功", response);
    }

    @Operation(summary = "发布日报")
    @PostMapping("/{reportId}/publish")
    public ApiResponse<DailyReportResponse> publishReport(Authentication authentication, @PathVariable Long reportId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        DailyReportResponse response = dailyReportService.publishReport(userDetails.getUsername(), reportId);
        return ApiResponse.success("日报发布成功", response);
    }

    @Operation(summary = "宝宝某日日报")
    @GetMapping("/baby/{babyId}")
    public ApiResponse<DailyReportResponse> getBabyReport(
            Authentication authentication,
            @PathVariable Long babyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        DailyReportResponse response = dailyReportService.getBabyReport(userDetails.getUsername(), babyId, date);
        return ApiResponse.success(response);
    }

    @Operation(summary = "宝宝日报列表")
    @GetMapping("/baby/{babyId}/list")
    public ApiResponse<List<DailyReportResponse>> getBabyReports(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<DailyReportResponse> response = dailyReportService.getBabyReports(userDetails.getUsername(), babyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "班级某日日报列表")
    @GetMapping("/classroom/{classroomId}")
    public ApiResponse<List<DailyReportResponse>> getClassroomReports(
            Authentication authentication,
            @PathVariable Long classroomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<DailyReportResponse> response = dailyReportService.getClassroomReports(
                userDetails.getUsername(),
                classroomId,
                date);
        return ApiResponse.success(response);
    }
}
