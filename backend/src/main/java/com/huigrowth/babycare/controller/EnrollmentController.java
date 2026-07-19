package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.EnrollmentBindByCodeRequest;
import com.huigrowth.babycare.dto.EnrollmentCreateRequest;
import com.huigrowth.babycare.dto.EnrollmentGuardianRequest;
import com.huigrowth.babycare.dto.EnrollmentGuardianResponse;
import com.huigrowth.babycare.dto.EnrollmentHealthCheckRequest;
import com.huigrowth.babycare.dto.EnrollmentReviewRequest;
import com.huigrowth.babycare.dto.EnrollmentResponse;
import com.huigrowth.babycare.dto.EnrollmentSupplementRequest;
import com.huigrowth.babycare.dto.EnrollmentSupplementResponse;
import com.huigrowth.babycare.dto.EnrollmentTransferRequest;
import com.huigrowth.babycare.dto.EnrollmentUpdateRequest;
import com.huigrowth.babycare.dto.EnrollmentWithdrawRequest;
import com.huigrowth.babycare.entity.EnrollmentStatusHistory;
import com.huigrowth.babycare.dto.MyEnrollmentResponse;
import com.huigrowth.babycare.service.EnrollmentService;
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

    // ========== 入托审核流程 ==========

    @Operation(summary = "审核入托申请", description = "园长审核入托申请，通过或驳回")
    @PostMapping("/{enrollmentId}/review")
    public ApiResponse<EnrollmentResponse> reviewEnrollment(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody EnrollmentReviewRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentResponse response = enrollmentService.reviewEnrollment(
                userDetails.getUsername(), enrollmentId, request);
        return ApiResponse.success("审核完成", response);
    }

    @Operation(summary = "转班", description = "将在托幼儿转到其他班级")
    @PostMapping("/{enrollmentId}/transfer")
    public ApiResponse<EnrollmentResponse> transferClassroom(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody EnrollmentTransferRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentResponse response = enrollmentService.transferClassroom(
                userDetails.getUsername(), enrollmentId, request);
        return ApiResponse.success("转班成功", response);
    }

    @Operation(summary = "退托", description = "办理退托/退学")
    @PostMapping("/{enrollmentId}/withdraw")
    public ApiResponse<EnrollmentResponse> withdrawEnrollment(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody EnrollmentWithdrawRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentResponse response = enrollmentService.withdrawEnrollment(
                userDetails.getUsername(), enrollmentId, request);
        return ApiResponse.success("退托成功", response);
    }

    // ========== 暂停与复托（T077） ==========

    @Operation(summary = "暂停入托", description = "暂停在托幼儿（ACTIVE → SUSPENDED）")
    @PostMapping("/{enrollmentId}/suspend")
    public ApiResponse<EnrollmentResponse> suspendEnrollment(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @RequestBody(required = false) EnrollmentWithdrawRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentResponse response = enrollmentService.suspendEnrollment(
                userDetails.getUsername(), enrollmentId, request != null ? request.getReason() : null);
        return ApiResponse.success("入托已暂停", response);
    }

    @Operation(summary = "复托", description = "恢复暂停的入托档案（SUSPENDED → ACTIVE）")
    @PostMapping("/{enrollmentId}/reactivate")
    public ApiResponse<EnrollmentResponse> reactivateEnrollment(
            Authentication authentication,
            @PathVariable Long enrollmentId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentResponse response = enrollmentService.reactivateEnrollment(
                userDetails.getUsername(), enrollmentId);
        return ApiResponse.success("复托成功", response);
    }

    @Operation(summary = "状态变更历史", description = "获取入托档案的状态变更历史记录")
    @GetMapping("/{enrollmentId}/history")
    public ApiResponse<List<EnrollmentStatusHistory>> getEnrollmentHistory(
            Authentication authentication,
            @PathVariable Long enrollmentId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<EnrollmentStatusHistory> response = enrollmentService.getEnrollmentHistory(
                userDetails.getUsername(), enrollmentId);
        return ApiResponse.success(response);
    }

    // ========== 入托保健审核（T074） ==========

    @Operation(summary = "保健审核", description = "保健员/保健医审核入托健康资料，通过或驳回")
    @PostMapping("/{enrollmentId}/health-check")
    public ApiResponse<EnrollmentResponse> healthCheckEnrollment(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody EnrollmentHealthCheckRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentResponse response = enrollmentService.healthCheckEnrollment(
                userDetails.getUsername(), enrollmentId, request);
        return ApiResponse.success(
                Boolean.TRUE.equals(request.getPassed()) ? "保健审核通过，正式入托" : "保健审核驳回",
                response);
    }

    @Operation(summary = "待保健审核列表", description = "获取机构下待保健审核的入托档案（保健员/保健医）")
    @GetMapping("/health-check-pending/{organizationId}")
    public ApiResponse<List<EnrollmentResponse>> getHealthCheckPendingEnrollments(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<EnrollmentResponse> response = enrollmentService.getHealthCheckPendingEnrollments(
                userDetails.getUsername(), organizationId);
        return ApiResponse.success(response);
    }

    // ========== 家长资料补充与确认（T076） ==========

    @Operation(summary = "获取补充资料状态", description = "获取家长补充资料的填写状态（T076）")
    @GetMapping("/{enrollmentId}/supplement")
    public ApiResponse<EnrollmentSupplementResponse> getSupplementStatus(
            Authentication authentication,
            @PathVariable Long enrollmentId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentSupplementResponse response = enrollmentService.getSupplementStatus(
                userDetails.getUsername(), enrollmentId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "补充入托资料", description = "家长补充宝宝身份、监护人身份、健康与紧急联系信息（T076）")
    @PutMapping("/{enrollmentId}/supplement")
    public ApiResponse<EnrollmentSupplementResponse> saveSupplement(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody EnrollmentSupplementRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentSupplementResponse response = enrollmentService.saveSupplement(
                userDetails.getUsername(), enrollmentId, request);
        return ApiResponse.success("资料保存成功", response);
    }

    @Operation(summary = "确认资料完整", description = "家长确认入托资料已补充完整，提交审核（T076）")
    @PostMapping("/{enrollmentId}/confirm")
    public ApiResponse<EnrollmentSupplementResponse> confirmSupplement(
            Authentication authentication,
            @PathVariable Long enrollmentId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentSupplementResponse response = enrollmentService.confirmSupplement(
                userDetails.getUsername(), enrollmentId);
        return ApiResponse.success("资料确认成功", response);
    }

    // ========== 家长/监护人绑定管理 ==========

    @Operation(summary = "添加入托监护人", description = "将用户绑定为入托档案的监护人（机构操作）")
    @PostMapping("/{enrollmentId}/guardians")
    public ApiResponse<EnrollmentGuardianResponse> addGuardian(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @Valid @RequestBody EnrollmentGuardianRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentGuardianResponse response = enrollmentService.addGuardian(
                userDetails.getUsername(), enrollmentId, request);
        return ApiResponse.success("监护人添加成功", response);
    }

    @Operation(summary = "移除入托监护人", description = "移除入托档案的监护人（机构操作）")
    @DeleteMapping("/{enrollmentId}/guardians/{guardianId}")
    public ApiResponse<String> removeGuardian(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @PathVariable Long guardianId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        enrollmentService.removeGuardian(userDetails.getUsername(), enrollmentId, guardianId);
        return ApiResponse.success("监护人移除成功");
    }

    @Operation(summary = "获取入托监护人列表", description = "获取指定入托档案的监护人列表")
    @GetMapping("/{enrollmentId}/guardians")
    public ApiResponse<List<EnrollmentGuardianResponse>> getEnrollmentGuardians(
            Authentication authentication,
            @PathVariable Long enrollmentId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<EnrollmentGuardianResponse> response = enrollmentService.getEnrollmentGuardians(
                userDetails.getUsername(), enrollmentId);
        return ApiResponse.success(response);
    }

    // ========== 家长视角 ==========

    @Operation(summary = "我的入托档案", description = "获取当前用户作为监护人的所有入托档案")
    @GetMapping("/my-enrollments")
    public ApiResponse<List<MyEnrollmentResponse>> getMyEnrollments(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<MyEnrollmentResponse> response = enrollmentService.getMyEnrollments(userDetails.getUsername());
        return ApiResponse.success(response);
    }

    // ========== 邀请码绑定 ==========

    @Operation(summary = "生成邀请码", description = "为入托档案生成家长绑定邀请码（机构操作）")
    @PostMapping("/{enrollmentId}/invite-code")
    public ApiResponse<String> generateInviteCode(
            Authentication authentication,
            @PathVariable Long enrollmentId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String code = enrollmentService.generateInviteCode(userDetails.getUsername(), enrollmentId);
        return ApiResponse.success("邀请码生成成功", code);
    }

    @Operation(summary = "通过邀请码绑定", description = "家长使用邀请码绑定入托档案")
    @PostMapping("/bind-by-code")
    public ApiResponse<EnrollmentGuardianResponse> bindByInviteCode(
            Authentication authentication,
            @Valid @RequestBody EnrollmentBindByCodeRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        EnrollmentGuardianResponse response = enrollmentService.bindByInviteCode(
                userDetails.getUsername(), request);
        return ApiResponse.success("绑定成功", response);
    }
}
