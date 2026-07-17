package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.ClassroomCreateRequest;
import com.huigrowth.babycare.dto.ClassroomResponse;
import com.huigrowth.babycare.dto.ClassroomUpdateRequest;
import com.huigrowth.babycare.service.ClassroomService;
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
 * 托育班级管理控制器
 */
@Tag(name = "托育班级管理", description = "班级创建、查询、更新等接口")
@RestController
@RequestMapping("/classroom")
@RequiredArgsConstructor
public class ClassroomController {

    private final ClassroomService classroomService;

    @Operation(summary = "创建班级", description = "在指定机构下创建一个新的托育班级")
    @PostMapping("/create")
    public ApiResponse<ClassroomResponse> createClassroom(
            Authentication authentication,
            @Valid @RequestBody ClassroomCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ClassroomResponse response = classroomService.createClassroom(userDetails.getUsername(), request);
        return ApiResponse.success("班级创建成功", response);
    }

    @Operation(summary = "获取机构班级列表", description = "获取指定机构下的所有班级")
    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<ClassroomResponse>> getOrganizationClassrooms(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<ClassroomResponse> response = classroomService.getOrganizationClassrooms(
                userDetails.getUsername(),
                organizationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取班级详情", description = "获取指定班级的详细信息")
    @GetMapping("/{classroomId}")
    public ApiResponse<ClassroomResponse> getClassroomDetail(
            Authentication authentication,
            @PathVariable Long classroomId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ClassroomResponse response = classroomService.getClassroomDetail(userDetails.getUsername(), classroomId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新班级", description = "更新指定班级的基础信息")
    @PutMapping("/{classroomId}")
    public ApiResponse<ClassroomResponse> updateClassroom(
            Authentication authentication,
            @PathVariable Long classroomId,
            @Valid @RequestBody ClassroomUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ClassroomResponse response = classroomService.updateClassroom(userDetails.getUsername(), classroomId, request);
        return ApiResponse.success("班级更新成功", response);
    }
}
