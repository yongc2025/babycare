package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.IncidentReportCreateRequest;
import com.huigrowth.babycare.dto.IncidentReportResponse;
import com.huigrowth.babycare.dto.IncidentReportUpdateRequest;
import com.huigrowth.babycare.service.IncidentReportService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@Tag(name = "异常与事故上报", description = "异常事件、事故处理、家长确认接口")
@RestController
@RequestMapping("/incident-report")
@RequiredArgsConstructor
public class IncidentReportController {

    private final IncidentReportService incidentReportService;

    @Operation(summary = "创建异常事故记录")
    @PostMapping("/create")
    public ApiResponse<IncidentReportResponse> createReport(
            Authentication authentication,
            @Valid @RequestBody IncidentReportCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        IncidentReportResponse response = incidentReportService.createReport(userDetails.getUsername(), request);
        return ApiResponse.success("异常事故记录创建成功", response);
    }

    @Operation(summary = "更新异常事故记录")
    @PutMapping("/{reportId}")
    public ApiResponse<IncidentReportResponse> updateReport(
            Authentication authentication,
            @PathVariable Long reportId,
            @Valid @RequestBody IncidentReportUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        IncidentReportResponse response = incidentReportService.updateReport(
                userDetails.getUsername(),
                reportId,
                request);
        return ApiResponse.success("异常事故记录更新成功", response);
    }

    @Operation(summary = "关闭异常事故记录")
    @PostMapping("/{reportId}/close")
    public ApiResponse<IncidentReportResponse> closeReport(Authentication authentication, @PathVariable Long reportId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        IncidentReportResponse response = incidentReportService.closeReport(userDetails.getUsername(), reportId);
        return ApiResponse.success("异常事故记录已关闭", response);
    }

    @Operation(summary = "家长确认异常事故记录")
    @PostMapping("/{reportId}/parent-confirm")
    public ApiResponse<IncidentReportResponse> confirmByParent(
            Authentication authentication,
            @PathVariable Long reportId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        IncidentReportResponse response = incidentReportService.confirmByParent(userDetails.getUsername(), reportId);
        return ApiResponse.success("家长确认成功", response);
    }

    @Operation(summary = "宝宝异常事故记录")
    @GetMapping("/baby/{babyId}")
    public ApiResponse<List<IncidentReportResponse>> getBabyReports(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<IncidentReportResponse> response = incidentReportService.getBabyReports(
                userDetails.getUsername(),
                babyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "班级异常事故记录")
    @GetMapping("/classroom/{classroomId}")
    public ApiResponse<List<IncidentReportResponse>> getClassroomReports(
            Authentication authentication,
            @PathVariable Long classroomId,
            @RequestParam(required = false) String status) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<IncidentReportResponse> response = incidentReportService.getClassroomReports(
                userDetails.getUsername(),
                classroomId,
                status);
        return ApiResponse.success(response);
    }
}
