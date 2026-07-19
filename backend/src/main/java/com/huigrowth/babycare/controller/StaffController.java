package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.StaffClassroomAssignmentRequest;
import com.huigrowth.babycare.dto.StaffClassroomAssignmentResponse;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    // ========== 员工-班级分配 ==========

    @Operation(summary = "分配员工到班级", description = "将教师/保育员分配到指定班级")
    @PostMapping("/assign-to-classroom")
    public ApiResponse<StaffClassroomAssignmentResponse> assignToClassroom(
            Authentication authentication,
            @Valid @RequestBody StaffClassroomAssignmentRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        StaffClassroomAssignmentResponse response = staffService.assignToClassroom(
                userDetails.getUsername(), request);
        return ApiResponse.success("分配成功", response);
    }

    @Operation(summary = "从班级移除员工", description = "将员工从指定班级中移除")
    @DeleteMapping("/classroom-assignment")
    public ApiResponse<String> removeFromClassroom(
            Authentication authentication,
            @RequestParam Long staffId,
            @RequestParam Long classroomId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        staffService.removeFromClassroom(userDetails.getUsername(), staffId, classroomId);
        return ApiResponse.success("移除成功");
    }

    @Operation(summary = "获取班级员工列表", description = "获取指定班级的教师/保育员分配列表")
    @GetMapping("/classroom-assignments/{classroomId}")
    public ApiResponse<List<StaffClassroomAssignmentResponse>> getClassroomAssignments(
            Authentication authentication,
            @PathVariable Long classroomId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<StaffClassroomAssignmentResponse> response = staffService.getClassroomAssignments(
                userDetails.getUsername(), classroomId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取员工班级分配", description = "获取指定员工分配的所有班级")
    @GetMapping("/staff-assignments/{staffId}")
    public ApiResponse<List<StaffClassroomAssignmentResponse>> getStaffAssignments(
            Authentication authentication,
            @PathVariable Long staffId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<StaffClassroomAssignmentResponse> response = staffService.getStaffAssignments(
                userDetails.getUsername(), staffId);
        return ApiResponse.success(response);
    }
}
