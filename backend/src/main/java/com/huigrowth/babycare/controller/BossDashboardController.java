package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.BossDashboardResponse;
import com.huigrowth.babycare.service.BossDashboardService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 老板/机构管理员多园区驾驶舱控制器
 */
@Tag(name = "老板驾驶舱", description = "多园区/多机构数据聚合看板")
@RestController
@RequestMapping("/boss/dashboard")
@RequiredArgsConstructor
public class BossDashboardController {

    private final BossDashboardService bossDashboardService;

    @Operation(summary = "获取多园区概览")
    @GetMapping("/overview")
    public ApiResponse<BossDashboardResponse> getOverview(@AuthenticationPrincipal UserDetails userDetails) {
        return ApiResponse.success(bossDashboardService.getOverview(userDetails.getUsername()));
    }
}
