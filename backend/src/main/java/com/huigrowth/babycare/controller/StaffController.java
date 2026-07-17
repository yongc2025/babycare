package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.StaffCreateRequest;
import com.huigrowth.babycare.dto.StaffResponse;
import com.huigrowth.babycare.dto.StaffUpdateRequest;
import com.huigrowth.babycare.service.StaffService;
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
 * 机构员工管理控制器
 */
@Tag(name = "机构员工管理", description = "员工创建、查询、更新等接口")
@RestController
@RequestMapping("/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @Operation(summary = "创建机构员工", description = "将已有用户绑定为指定机构员工")
    @PostMapping("/create")
    public ApiResponse<StaffResponse> createStaff(
            Authentication authentication,
            @Valid @RequestBody StaffCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        StaffResponse response = staffService.createStaff(userDetails.getUsername(), request);
        return ApiResponse.success("员工创建成功", response);
    }

    @Operation(summary = "获取机构员工列表", description = "获取指定机构下的员工列表")
    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<StaffResponse>> getOrganizationStaff(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<StaffResponse> response = staffService.getOrganizationStaff(
                userDetails.getUsername(),
                organizationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取员工详情", description = "获取指定机构员工的详细信息")
    @GetMapping("/{staffId}")
    public ApiResponse<StaffResponse> getStaffDetail(
            Authentication authentication,
            @PathVariable Long staffId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        StaffResponse response = staffService.getStaffDetail(userDetails.getUsername(), staffId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新机构员工", description = "更新员工角色或状态")
    @PutMapping("/{staffId}")
    public ApiResponse<StaffResponse> updateStaff(
            Authentication authentication,
            @PathVariable Long staffId,
            @Valid @RequestBody StaffUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        StaffResponse response = staffService.updateStaff(userDetails.getUsername(), staffId, request);
        return ApiResponse.success("员工更新成功", response);
    }
}
