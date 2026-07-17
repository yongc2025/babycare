package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.PickupDelegationCreateRequest;
import com.huigrowth.babycare.dto.PickupDelegationResponse;
import com.huigrowth.babycare.dto.PickupDelegationReviewRequest;
import com.huigrowth.babycare.dto.PickupPersonCreateRequest;
import com.huigrowth.babycare.dto.PickupPersonResponse;
import com.huigrowth.babycare.dto.PickupPersonUpdateRequest;
import com.huigrowth.babycare.service.PickupService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@Tag(name = "接送授权与委托", description = "授权接送人和临时委托接送接口")
@RestController
@RequestMapping("/pickup")
@RequiredArgsConstructor
public class PickupController {

    private final PickupService pickupService;

    @Operation(summary = "新增授权接送人")
    @PostMapping("/person/create")
    public ApiResponse<PickupPersonResponse> createPickupPerson(
            Authentication authentication,
            @Valid @RequestBody PickupPersonCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        PickupPersonResponse response = pickupService.createPickupPerson(userDetails.getUsername(), request);
        return ApiResponse.success("授权接送人创建成功", response);
    }

    @Operation(summary = "更新授权接送人")
    @PutMapping("/person/{personId}")
    public ApiResponse<PickupPersonResponse> updatePickupPerson(
            Authentication authentication,
            @PathVariable Long personId,
            @Valid @RequestBody PickupPersonUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        PickupPersonResponse response = pickupService.updatePickupPerson(
                userDetails.getUsername(),
                personId,
                request);
        return ApiResponse.success("授权接送人更新成功", response);
    }

    @Operation(summary = "宝宝授权接送人列表")
    @GetMapping("/person/baby/{babyId}")
    public ApiResponse<List<PickupPersonResponse>> getBabyPickupPersons(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<PickupPersonResponse> response = pickupService.getBabyPickupPersons(userDetails.getUsername(), babyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "班级授权接送人列表")
    @GetMapping("/person/classroom/{classroomId}")
    public ApiResponse<List<PickupPersonResponse>> getClassroomPickupPersons(
            Authentication authentication,
            @PathVariable Long classroomId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<PickupPersonResponse> response = pickupService.getClassroomPickupPersons(
                userDetails.getUsername(),
                classroomId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "创建临时委托接送申请")
    @PostMapping("/delegation/create")
    public ApiResponse<PickupDelegationResponse> createDelegation(
            Authentication authentication,
            @Valid @RequestBody PickupDelegationCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        PickupDelegationResponse response = pickupService.createDelegation(userDetails.getUsername(), request);
        return ApiResponse.success("委托接送申请创建成功", response);
    }

    @Operation(summary = "审核通过委托接送")
    @PostMapping("/delegation/{delegationId}/approve")
    public ApiResponse<PickupDelegationResponse> approveDelegation(
            Authentication authentication,
            @PathVariable Long delegationId,
            @Valid @RequestBody PickupDelegationReviewRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        PickupDelegationResponse response = pickupService.approveDelegation(
                userDetails.getUsername(),
                delegationId,
                request);
        return ApiResponse.success("委托接送审核通过", response);
    }

    @Operation(summary = "审核拒绝委托接送")
    @PostMapping("/delegation/{delegationId}/reject")
    public ApiResponse<PickupDelegationResponse> rejectDelegation(
            Authentication authentication,
            @PathVariable Long delegationId,
            @Valid @RequestBody PickupDelegationReviewRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        PickupDelegationResponse response = pickupService.rejectDelegation(
                userDetails.getUsername(),
                delegationId,
                request);
        return ApiResponse.success("委托接送审核拒绝", response);
    }

    @Operation(summary = "宝宝委托接送列表")
    @GetMapping("/delegation/baby/{babyId}")
    public ApiResponse<List<PickupDelegationResponse>> getBabyDelegations(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<PickupDelegationResponse> response = pickupService.getBabyDelegations(userDetails.getUsername(), babyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "班级某日委托接送列表")
    @GetMapping("/delegation/classroom/{classroomId}")
    public ApiResponse<List<PickupDelegationResponse>> getClassroomDelegations(
            Authentication authentication,
            @PathVariable Long classroomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<PickupDelegationResponse> response = pickupService.getClassroomDelegations(
                userDetails.getUsername(),
                classroomId,
                date);
        return ApiResponse.success(response);
    }
}
