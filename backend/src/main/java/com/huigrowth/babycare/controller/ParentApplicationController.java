package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.BillingStatementResponse;
import com.huigrowth.babycare.dto.CancelApplicationRequest;
import com.huigrowth.babycare.dto.LeaveRequestCreateRequest;
import com.huigrowth.babycare.dto.LeaveRequestResponse;
import com.huigrowth.babycare.dto.MedicationRequestCreateRequest;
import com.huigrowth.babycare.dto.MedicationRequestResponse;
import com.huigrowth.babycare.dto.ParentApplicationResponse;
import com.huigrowth.babycare.dto.PickupDelegationCreateRequest;
import com.huigrowth.babycare.dto.PickupDelegationResponse;
import com.huigrowth.babycare.entity.EnrollmentGuardian;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.repository.EnrollmentGuardianRepository;
import com.huigrowth.babycare.repository.UserRepository;
import com.huigrowth.babycare.service.AttendanceService;
import com.huigrowth.babycare.service.BillingService;
import com.huigrowth.babycare.service.MedicationCareService;
import com.huigrowth.babycare.service.PickupService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 家长申请控制器
 * 提供家长视角的统一申请提交、查看和取消功能
 */
@Tag(name = "家长申请", description = "家长请假、用药、接送委托的统一提交、查看和取消接口")
@RestController
@RequestMapping("/parent")
@RequiredArgsConstructor
public class ParentApplicationController {

    private final AttendanceService attendanceService;
    private final MedicationCareService medicationCareService;
    private final PickupService pickupService;
    private final BillingService billingService;
    private final EnrollmentGuardianRepository enrollmentGuardianRepository;
    private final UserRepository userRepository;

    @Operation(summary = "我的申请列表", description = "获取当前用户所有请假、用药委托和接送委托申请")
    @GetMapping("/my-applications")
    public ApiResponse<List<ParentApplicationResponse>> getMyApplications(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<ParentApplicationResponse> result = new ArrayList<>();

        // 获取当前用户的所有入托监护人关系
        List<EnrollmentGuardian> guardians = enrollmentGuardianRepository.findByGuardianUserOrderByCreatedAtDesc(user);

        // 收集所有宝宝ID
        List<Long> babyIds = guardians.stream()
                .map(g -> g.getEnrollment().getBaby().getId())
                .distinct()
                .toList();

        // 查询请假申请
        List<LeaveRequestResponse> leaveRequests = attendanceService.getMyLeaveRequests(userDetails.getUsername());
        for (LeaveRequestResponse lr : leaveRequests) {
            ParentApplicationResponse app = ParentApplicationResponse.builder()
                    .id(lr.getId())
                    .applicationType("LEAVE")
                    .applicationTypeDescription("请假申请")
                    .babyId(lr.getBabyId())
                    .babyName(lr.getBabyName())
                    .enrollmentId(lr.getEnrollmentId())
                    .classroomId(lr.getClassroomId())
                    .classroomName(lr.getClassroomName())
                    .organizationId(lr.getOrganizationId())
                    .organizationName(lr.getOrganizationName())
                    .status(lr.getStatus() != null ? lr.getStatus().name() : null)
                    .statusDescription(lr.getStatusDescription())
                    .reason(lr.getReason())
                    .requestedByName(lr.getRequestedByName())
                    .reviewedByName(lr.getReviewedByName())
                    .reviewRemark(lr.getReviewRemark())
                    .reviewedAt(lr.getReviewedAt())
                    .createdAt(lr.getCreatedAt())
                    .leaveStartDate(lr.getStartDate())
                    .leaveEndDate(lr.getEndDate())
                    .leaveType(lr.getType() != null ? lr.getType().name() : null)
                    .leaveTypeDescription(lr.getTypeDescription())
                    .build();
            result.add(app);
        }

        // 查询用药委托
        List<MedicationRequestResponse> medications = medicationCareService.getMyMedicationRequests(userDetails.getUsername());
        for (MedicationRequestResponse mr : medications) {
            ParentApplicationResponse app = ParentApplicationResponse.builder()
                    .id(mr.getId())
                    .applicationType("MEDICATION")
                    .applicationTypeDescription("用药委托")
                    .babyId(mr.getBabyId())
                    .babyName(mr.getBabyName())
                    .enrollmentId(mr.getEnrollmentId())
                    .classroomId(mr.getClassroomId())
                    .classroomName(mr.getClassroomName())
                    .organizationId(mr.getOrganizationId())
                    .organizationName(mr.getOrganizationName())
                    .status(mr.getStatus() != null ? mr.getStatus().name() : null)
                    .statusDescription(mr.getStatusDescription())
                    .requestedByName(mr.getRequestedByName())
                    .reviewedByName(mr.getReviewedByName())
                    .reviewRemark(mr.getReviewRemark())
                    .reviewedAt(mr.getReviewedAt())
                    .createdAt(mr.getCreatedAt())
                    .medicineName(mr.getMedicineName())
                    .dosage(mr.getDosage())
                    .frequency(mr.getFrequency())
                    .medicationStartDate(mr.getStartDate())
                    .medicationEndDate(mr.getEndDate())
                    .instructions(mr.getInstructions())
                    .build();
            result.add(app);
        }

        // 查询接送委托
        List<PickupDelegationResponse> delegations = pickupService.getMyDelegations(userDetails.getUsername());
        for (PickupDelegationResponse pd : delegations) {
            ParentApplicationResponse app = ParentApplicationResponse.builder()
                    .id(pd.getId())
                    .applicationType("PICKUP")
                    .applicationTypeDescription("接送委托")
                    .babyId(pd.getBabyId())
                    .babyName(pd.getBabyName())
                    .enrollmentId(pd.getEnrollmentId())
                    .classroomId(pd.getClassroomId())
                    .classroomName(pd.getClassroomName())
                    .organizationId(pd.getOrganizationId())
                    .organizationName(pd.getOrganizationName())
                    .status(pd.getStatus() != null ? pd.getStatus().name() : null)
                    .statusDescription(pd.getStatusDescription())
                    .reason(pd.getReason())
                    .requestedByName(pd.getRequestedByName())
                    .reviewedByName(pd.getReviewedByName())
                    .reviewRemark(pd.getReviewRemark())
                    .reviewedAt(pd.getReviewedAt())
                    .createdAt(pd.getCreatedAt())
                    .pickupDate(pd.getPickupDate())
                    .pickupPersonName(pd.getPickupPersonName())
                    .pickupRelationship(pd.getPickupRelationship())
                    .pickupPhone(pd.getPickupPhone())
                    .pickupCode(pd.getPickupCode())
                    .build();
            result.add(app);
        }

        // 按创建时间降序排列
        result.sort(Comparator.comparing(ParentApplicationResponse::getCreatedAt).reversed());

        return ApiResponse.success(result);
    }

    @Operation(summary = "提交请假申请", description = "家长提交请假申请")
    @PostMapping("/leave-request")
    public ApiResponse<LeaveRequestResponse> createLeaveRequest(
            Authentication authentication,
            @Valid @RequestBody LeaveRequestCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        LeaveRequestResponse response = attendanceService.createLeaveRequest(userDetails.getUsername(), request);
        return ApiResponse.success("请假申请提交成功", response);
    }

    @Operation(summary = "提交用药委托", description = "家长提交用药委托")
    @PostMapping("/medication-request")
    public ApiResponse<MedicationRequestResponse> createMedicationRequest(
            Authentication authentication,
            @Valid @RequestBody MedicationRequestCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        MedicationRequestResponse response = medicationCareService.createMedicationRequest(
                userDetails.getUsername(), request);
        return ApiResponse.success("用药委托提交成功", response);
    }

    @Operation(summary = "提交接送委托", description = "家长提交临时接送委托")
    @PostMapping("/pickup-delegation")
    public ApiResponse<PickupDelegationResponse> createPickupDelegation(
            Authentication authentication,
            @Valid @RequestBody PickupDelegationCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        PickupDelegationResponse response = pickupService.createDelegation(userDetails.getUsername(), request);
        return ApiResponse.success("接送委托提交成功", response);
    }

    @Operation(summary = "我的账单列表", description = "获取当前用户所有入托宝宝的账单")
    @GetMapping("/my-bills")
    public ApiResponse<List<BillingStatementResponse>> getMyBills(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<BillingStatementResponse> response = billingService.getMyBills(userDetails.getUsername());
        return ApiResponse.success(response);
    }

    @Operation(summary = "取消申请", description = "取消待审核的请假/用药/接送委托申请")
    @PostMapping("/cancel-application")
    public ApiResponse<?> cancelApplication(
            Authentication authentication,
            @Valid @RequestBody CancelApplicationRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        switch (request.getApplicationType()) {
            case "LEAVE" -> {
                LeaveRequestResponse response = attendanceService.cancelLeaveRequest(
                        username, request.getApplicationId(), request.getReason());
                return ApiResponse.success("请假申请已取消", response);
            }
            case "MEDICATION" -> {
                MedicationRequestResponse response = medicationCareService.cancelMedicationRequest(
                        username, request.getApplicationId(), request.getReason());
                return ApiResponse.success("用药委托已取消", response);
            }
            case "PICKUP" -> {
                PickupDelegationResponse response = pickupService.cancelDelegation(
                        username, request.getApplicationId(), request.getReason());
                return ApiResponse.success("接送委托已取消", response);
            }
            default -> throw new IllegalArgumentException("不支持的申请类型: " + request.getApplicationType());
        }
    }
}
