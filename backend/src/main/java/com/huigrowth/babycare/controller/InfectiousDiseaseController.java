package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.InfectiousDiseaseCreateRequest;
import com.huigrowth.babycare.dto.InfectiousDiseaseResponse;
import com.huigrowth.babycare.dto.InfectiousDiseaseUpdateRequest;
import com.huigrowth.babycare.service.InfectiousDiseaseService;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "传染病防控", description = "传染病疑似、确诊、隔离、复园全流程管理")
@RestController
@RequestMapping("/infectious-disease")
@RequiredArgsConstructor
public class InfectiousDiseaseController {

    private final InfectiousDiseaseService infectiousDiseaseService;

    @Operation(summary = "创建传染病记录")
    @PostMapping("/create")
    public ApiResponse<InfectiousDiseaseResponse> createRecord(
            Authentication authentication,
            @Valid @RequestBody InfectiousDiseaseCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        InfectiousDiseaseResponse response = infectiousDiseaseService.createRecord(
                userDetails.getUsername(), request);
        return ApiResponse.success("传染病记录创建成功", response);
    }

    @Operation(summary = "更新传染病记录")
    @PutMapping("/{recordId}")
    public ApiResponse<InfectiousDiseaseResponse> updateRecord(
            Authentication authentication,
            @PathVariable Long recordId,
            @Valid @RequestBody InfectiousDiseaseUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        InfectiousDiseaseResponse response = infectiousDiseaseService.updateRecord(
                userDetails.getUsername(), recordId, request);
        return ApiResponse.success("传染病记录更新成功", response);
    }

    @Operation(summary = "班级传染病记录列表")
    @GetMapping("/classroom/{classroomId}")
    public ApiResponse<List<InfectiousDiseaseResponse>> getClassroomRecords(
            Authentication authentication,
            @PathVariable Long classroomId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<InfectiousDiseaseResponse> response = infectiousDiseaseService.getClassroomRecords(
                userDetails.getUsername(), classroomId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "机构传染病记录列表")
    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<InfectiousDiseaseResponse>> getOrganizationRecords(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<InfectiousDiseaseResponse> response = infectiousDiseaseService.getOrganizationRecords(
                userDetails.getUsername(), organizationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "传染病记录详情")
    @GetMapping("/{recordId}")
    public ApiResponse<InfectiousDiseaseResponse> getRecordDetail(
            Authentication authentication,
            @PathVariable Long recordId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        InfectiousDiseaseResponse response = infectiousDiseaseService.getRecordDetail(
                userDetails.getUsername(), recordId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "机构活跃传染病数")
    @GetMapping("/organization/{organizationId}/active-count")
    public ApiResponse<Long> getActiveCount(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        long count = infectiousDiseaseService.countActiveByOrganization(
                userDetails.getUsername(), organizationId);
        return ApiResponse.success(count);
    }
}
