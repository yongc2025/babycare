package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.DirectorDashboardResponse;
import com.huigrowth.babycare.service.DirectorDashboardService;
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

@Tag(name = "园长驾驶舱", description = "机构运营关键指标聚合接口")
@RestController
@RequestMapping("/director-dashboard")
@RequiredArgsConstructor
public class DirectorDashboardController {

    private final DirectorDashboardService directorDashboardService;

    @Operation(summary = "机构运营概览", description = "按机构和日期汇总在托人数、出勤、异常、账单和公告指标")
    @GetMapping("/organization/{organizationId}")
    public ApiResponse<DirectorDashboardResponse> getOrganizationOverview(
            Authentication authentication,
            @PathVariable Long organizationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        DirectorDashboardResponse response = directorDashboardService.getOverview(
                userDetails.getUsername(),
                organizationId,
                date);
        return ApiResponse.success(response);
    }
}
