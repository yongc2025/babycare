package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.RegulatoryExportRow;
import com.huigrowth.babycare.dto.RegulatoryReportResponse;
import com.huigrowth.babycare.service.RegulatoryReportService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "监管报表", description = "机构监管字段聚合和导出数据接口")
@RestController
@RequestMapping("/regulatory-report")
@RequiredArgsConstructor
public class RegulatoryReportController {

    private final RegulatoryReportService regulatoryReportService;

    @Operation(summary = "机构监管报表概览", description = "按机构和日期范围聚合备案、规模、人员、健康和安全卫生数据")
    @GetMapping("/organization/{organizationId}")
    public ApiResponse<RegulatoryReportResponse> getOrganizationReport(
            Authentication authentication,
            @PathVariable Long organizationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        RegulatoryReportResponse response = regulatoryReportService.getOrganizationReport(
                userDetails.getUsername(),
                organizationId,
                startDate,
                endDate);
        return ApiResponse.success(response);
    }

    @Operation(summary = "机构监管导出行", description = "返回可用于后续 CSV/Excel 的扁平化监管字段行")
    @GetMapping("/organization/{organizationId}/export-rows")
    public ApiResponse<List<RegulatoryExportRow>> getOrganizationExportRows(
            Authentication authentication,
            @PathVariable Long organizationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        RegulatoryReportResponse response = regulatoryReportService.getOrganizationReport(
                userDetails.getUsername(),
                organizationId,
                startDate,
                endDate);
        return ApiResponse.success(response.getExportRows());
    }
}
