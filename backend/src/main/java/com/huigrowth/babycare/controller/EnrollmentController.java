package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.EnrollmentCreateRequest;
import com.huigrowth.babycare.dto.EnrollmentResponse;
import com.huigrowth.babycare.dto.EnrollmentUpdateRequest;
import com.huigrowth.babycare.service.EnrollmentService;
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
 * 宝宝入托档案控制器
 */
@Tag(name = "宝宝入托档案", description = "宝宝入托、班级归属和基础健康资料接口")
@RestController
@RequestMapping("/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @Operation(summary = "创建入托档案", description = "为宝宝创建机构和班级入托关系")
    @PostMapping("/create")
    public ApiResponse<EnrollmentResponse> createEnrollment(
            Authentication authentication,
            @Valid @RequestBody EnrollmentCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentResponse response = enrollmentService.createEnrollment(userDetails.getUsername(), request);
        return ApiResponse.success("入托档案创建成功", response);
    }

    @Operation(summary = "获取班级入托档案", description = "获取指定班级下的入托宝宝列表")
    @GetMapping("/classroom/{classroomId}")
    public ApiResponse<List<EnrollmentResponse>> getClassroomEnrollments(
            Authentication authentication,
            @PathVariable Long classroomId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<EnrollmentResponse> response = enrollmentService.getClassroomEnrollments(
                userDetails.getUsername(),
                classroomId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取宝宝入托档案", description = "获取指定宝宝的入托记录")
    @GetMapping("/baby/{babyId}")
    public ApiResponse<List<EnrollmentResponse>> getBabyEnrollments(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<EnrollmentResponse> response = enrollmentService.getBabyEnrollments(userDetails.getUsername(), babyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取入托档案详情", description = "获取指定入托档案详情")
    @GetMapping("/{enrollmentId}")
    public ApiResponse<EnrollmentResponse> getEnrollmentDetail(
            Authentication authentication,
            @PathVariable Long enrollmentId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentResponse response = enrollmentService.getEnrollmentDetail(userDetails.getUsername(), enrollmentId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新入托档案", description = "更新宝宝班级、入托状态或基础健康资料")
    @PutMapping("/{enrollmentId}")
    public ApiResponse<EnrollmentResponse> updateEnrollment(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody EnrollmentUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentResponse response = enrollmentService.updateEnrollment(userDetails.getUsername(), enrollmentId, request);
        return ApiResponse.success("入托档案更新成功", response);
    }
}
