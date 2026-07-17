package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.ChildDevelopmentAssessmentRequest;
import com.huigrowth.babycare.dto.ChildDevelopmentAssessmentResponse;
import com.huigrowth.babycare.service.ChildDevelopmentAssessmentService;
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

@Tag(name = "儿童发展评估", description = "月龄里程碑、五大领域评估和雷达图数据接口")
@RestController
@RequestMapping("/child-development-assessment")
@RequiredArgsConstructor
public class ChildDevelopmentAssessmentController {

    private final ChildDevelopmentAssessmentService assessmentService;

    @Operation(summary = "创建发展评估")
    @PostMapping("/create")
    public ApiResponse<ChildDevelopmentAssessmentResponse> createAssessment(
            Authentication authentication,
            @Valid @RequestBody ChildDevelopmentAssessmentRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ChildDevelopmentAssessmentResponse response = assessmentService.createAssessment(
                userDetails.getUsername(),
                request);
        return ApiResponse.success("发展评估创建成功", response);
    }

    @Operation(summary = "更新发展评估")
    @PutMapping("/{assessmentId}")
    public ApiResponse<ChildDevelopmentAssessmentResponse> updateAssessment(
            Authentication authentication,
            @PathVariable Long assessmentId,
            @Valid @RequestBody ChildDevelopmentAssessmentRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ChildDevelopmentAssessmentResponse response = assessmentService.updateAssessment(
                userDetails.getUsername(),
                assessmentId,
                request);
        return ApiResponse.success("发展评估更新成功", response);
    }

    @Operation(summary = "宝宝发展评估历史")
    @GetMapping("/baby/{babyId}")
    public ApiResponse<List<ChildDevelopmentAssessmentResponse>> getBabyAssessments(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<ChildDevelopmentAssessmentResponse> response = assessmentService.getBabyAssessments(
                userDetails.getUsername(),
                babyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "入托档案发展评估历史")
    @GetMapping("/enrollment/{enrollmentId}")
    public ApiResponse<List<ChildDevelopmentAssessmentResponse>> getEnrollmentAssessments(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @RequestParam(required = false) String assessmentMode) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<ChildDevelopmentAssessmentResponse> response = assessmentService.getEnrollmentAssessments(
                userDetails.getUsername(),
                enrollmentId,
                assessmentMode);
        return ApiResponse.success(response);
    }

    @Operation(summary = "发展评估详情")
    @GetMapping("/{assessmentId}")
    public ApiResponse<ChildDevelopmentAssessmentResponse> getAssessmentDetail(
            Authentication authentication,
            @PathVariable Long assessmentId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        ChildDevelopmentAssessmentResponse response = assessmentService.getAssessmentDetail(
                userDetails.getUsername(),
                assessmentId);
        return ApiResponse.success(response);
    }
}
