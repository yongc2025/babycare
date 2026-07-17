package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.AllergyTagRequest;
import com.huigrowth.babycare.dto.AllergyTagResponse;
import com.huigrowth.babycare.dto.MedicationAdministrationRequest;
import com.huigrowth.babycare.dto.MedicationAdministrationResponse;
import com.huigrowth.babycare.dto.MedicationRequestCreateRequest;
import com.huigrowth.babycare.dto.MedicationRequestResponse;
import com.huigrowth.babycare.dto.MedicationReviewRequest;
import com.huigrowth.babycare.service.MedicationCareService;
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

@Tag(name = "用药与过敏管理", description = "过敏标签、用药委托、用药审核和执行记录接口")
@RestController
@RequestMapping("/medication-care")
@RequiredArgsConstructor
public class MedicationCareController {

    private final MedicationCareService medicationCareService;

    @Operation(summary = "创建过敏标签")
    @PostMapping("/allergy/create")
    public ApiResponse<AllergyTagResponse> createAllergyTag(
            Authentication authentication,
            @Valid @RequestBody AllergyTagRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AllergyTagResponse response = medicationCareService.createAllergyTag(userDetails.getUsername(), request);
        return ApiResponse.success("过敏标签创建成功", response);
    }

    @Operation(summary = "更新过敏标签")
    @PutMapping("/allergy/{allergyId}")
    public ApiResponse<AllergyTagResponse> updateAllergyTag(
            Authentication authentication,
            @PathVariable Long allergyId,
            @Valid @RequestBody AllergyTagRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AllergyTagResponse response = medicationCareService.updateAllergyTag(
                userDetails.getUsername(),
                allergyId,
                request);
        return ApiResponse.success("过敏标签更新成功", response);
    }

    @Operation(summary = "宝宝过敏标签列表")
    @GetMapping("/allergy/baby/{babyId}")
    public ApiResponse<List<AllergyTagResponse>> getBabyAllergies(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<AllergyTagResponse> response = medicationCareService.getBabyAllergies(
                userDetails.getUsername(),
                babyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "创建用药委托")
    @PostMapping("/request/create")
    public ApiResponse<MedicationRequestResponse> createMedicationRequest(
            Authentication authentication,
            @Valid @RequestBody MedicationRequestCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        MedicationRequestResponse response = medicationCareService.createMedicationRequest(
                userDetails.getUsername(),
                request);
        return ApiResponse.success("用药委托创建成功", response);
    }

    @Operation(summary = "审核通过用药委托")
    @PostMapping("/request/{medicationRequestId}/approve")
    public ApiResponse<MedicationRequestResponse> approveMedication(
            Authentication authentication,
            @PathVariable Long medicationRequestId,
            @Valid @RequestBody MedicationReviewRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        MedicationRequestResponse response = medicationCareService.approveMedication(
                userDetails.getUsername(),
                medicationRequestId,
                request);
        return ApiResponse.success("用药委托审核通过", response);
    }

    @Operation(summary = "审核拒绝用药委托")
    @PostMapping("/request/{medicationRequestId}/reject")
    public ApiResponse<MedicationRequestResponse> rejectMedication(
            Authentication authentication,
            @PathVariable Long medicationRequestId,
            @Valid @RequestBody MedicationReviewRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        MedicationRequestResponse response = medicationCareService.rejectMedication(
                userDetails.getUsername(),
                medicationRequestId,
                request);
        return ApiResponse.success("用药委托审核拒绝", response);
    }

    @Operation(summary = "记录用药执行")
    @PostMapping("/administration/create")
    public ApiResponse<MedicationAdministrationResponse> recordAdministration(
            Authentication authentication,
            @Valid @RequestBody MedicationAdministrationRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        MedicationAdministrationResponse response = medicationCareService.recordAdministration(
                userDetails.getUsername(),
                request);
        return ApiResponse.success("用药执行记录成功", response);
    }

    @Operation(summary = "宝宝用药委托列表")
    @GetMapping("/request/baby/{babyId}")
    public ApiResponse<List<MedicationRequestResponse>> getBabyMedications(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<MedicationRequestResponse> response = medicationCareService.getBabyMedications(
                userDetails.getUsername(),
                babyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "班级用药委托列表")
    @GetMapping("/request/classroom/{classroomId}")
    public ApiResponse<List<MedicationRequestResponse>> getClassroomMedications(
            Authentication authentication,
            @PathVariable Long classroomId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<MedicationRequestResponse> response = medicationCareService.getClassroomMedications(
                userDetails.getUsername(),
                classroomId);
        return ApiResponse.success(response);
    }
}
