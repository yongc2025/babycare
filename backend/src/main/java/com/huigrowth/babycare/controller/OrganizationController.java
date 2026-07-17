package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.OrganizationCreateRequest;
import com.huigrowth.babycare.dto.OrganizationResponse;
import com.huigrowth.babycare.dto.OrganizationUpdateRequest;
import com.huigrowth.babycare.service.OrganizationService;
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

/**
 * 托育机构管理控制器
 */
@Tag(name = "托育机构管理", description = "机构创建、查询、更新等接口")
@RestController
@RequestMapping("/organization")
@RequiredArgsConstructor
public class OrganizationController {

    private final OrganizationService organizationService;

    @Operation(summary = "创建机构", description = "创建一个新的托育机构")
    @PostMapping("/create")
    public ApiResponse<OrganizationResponse> createOrganization(
            Authentication authentication,
            @Valid @RequestBody OrganizationCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        OrganizationResponse response = organizationService.createOrganization(userDetails.getUsername(), request);
        return ApiResponse.success("机构创建成功", response);
    }

    @Operation(summary = "获取我的机构列表", description = "获取当前用户创建的托育机构")
    @GetMapping("/my-organizations")
    public ApiResponse<List<OrganizationResponse>> getMyOrganizations(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<OrganizationResponse> response = organizationService.getMyOrganizations(userDetails.getUsername());
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取机构详情", description = "获取指定托育机构的详细信息")
    @GetMapping("/{organizationId}")
    public ApiResponse<OrganizationResponse> getOrganizationDetail(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        OrganizationResponse response = organizationService.getOrganizationDetail(userDetails.getUsername(), organizationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新机构", description = "更新指定托育机构的基础信息")
    @PutMapping("/{organizationId}")
    public ApiResponse<OrganizationResponse> updateOrganization(
            Authentication authentication,
            @PathVariable Long organizationId,
            @Valid @RequestBody OrganizationUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        OrganizationResponse response = organizationService.updateOrganization(userDetails.getUsername(), organizationId, request);
        return ApiResponse.success("机构更新成功", response);
    }
}
