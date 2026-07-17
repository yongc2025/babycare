package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.AdmissionLeadRequest;
import com.huigrowth.babycare.dto.AdmissionLeadResponse;
import com.huigrowth.babycare.dto.AdmissionReviewRequest;
import com.huigrowth.babycare.dto.AdmissionTrialRequest;
import com.huigrowth.babycare.service.AdmissionLeadService;
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

@Tag(name = "招生线索", description = "意向登记、报名审核和试托状态管理接口")
@RestController
@RequestMapping("/admission-lead")
@RequiredArgsConstructor
public class AdmissionLeadController {

    private final AdmissionLeadService admissionLeadService;

    @Operation(summary = "创建招生线索")
    @PostMapping("/create")
    public ApiResponse<AdmissionLeadResponse> createLead(
            Authentication authentication,
            @Valid @RequestBody AdmissionLeadRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AdmissionLeadResponse response = admissionLeadService.createLead(userDetails.getUsername(), request);
        return ApiResponse.success("招生线索创建成功", response);
    }

    @Operation(summary = "更新招生线索")
    @PutMapping("/{leadId}")
    public ApiResponse<AdmissionLeadResponse> updateLead(
            Authentication authentication,
            @PathVariable Long leadId,
            @Valid @RequestBody AdmissionLeadRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AdmissionLeadResponse response = admissionLeadService.updateLead(
                userDetails.getUsername(),
                leadId,
                request);
        return ApiResponse.success("招生线索更新成功", response);
    }

    @Operation(summary = "报名审核")
    @PostMapping("/{leadId}/review")
    public ApiResponse<AdmissionLeadResponse> reviewApplication(
            Authentication authentication,
            @PathVariable Long leadId,
            @Valid @RequestBody AdmissionReviewRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AdmissionLeadResponse response = admissionLeadService.reviewApplication(
                userDetails.getUsername(),
                leadId,
                request);
        return ApiResponse.success("报名审核完成", response);
    }

    @Operation(summary = "开始试托")
    @PostMapping("/{leadId}/trial/start")
    public ApiResponse<AdmissionLeadResponse> startTrial(
            Authentication authentication,
            @PathVariable Long leadId,
            @Valid @RequestBody AdmissionTrialRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AdmissionLeadResponse response = admissionLeadService.startTrial(
                userDetails.getUsername(),
                leadId,
                request);
        return ApiResponse.success("试托已开始", response);
    }

    @Operation(summary = "结束试托")
    @PostMapping("/{leadId}/trial/finish")
    public ApiResponse<AdmissionLeadResponse> finishTrial(
            Authentication authentication,
            @PathVariable Long leadId,
            @Valid @RequestBody AdmissionTrialRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AdmissionLeadResponse response = admissionLeadService.finishTrial(
                userDetails.getUsername(),
                leadId,
                request);
        return ApiResponse.success("试托已完成", response);
    }

    @Operation(summary = "机构招生线索列表")
    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<AdmissionLeadResponse>> getOrganizationLeads(
            Authentication authentication,
            @PathVariable Long organizationId,
            @RequestParam(required = false) String status) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<AdmissionLeadResponse> response = admissionLeadService.getOrganizationLeads(
                userDetails.getUsername(),
                organizationId,
                status);
        return ApiResponse.success(response);
    }

    @Operation(summary = "招生线索详情")
    @GetMapping("/{leadId}")
    public ApiResponse<AdmissionLeadResponse> getLeadDetail(
            Authentication authentication,
            @PathVariable Long leadId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AdmissionLeadResponse response = admissionLeadService.getLeadDetail(userDetails.getUsername(), leadId);
        return ApiResponse.success(response);
    }
}
