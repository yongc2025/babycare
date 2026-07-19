package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.*;
import com.huigrowth.babycare.service.FamilyService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 家庭管理控制器
 * 
 * @author HuiGrowth Team
 */
@Tag(name = "家庭管理", description = "家庭创建、加入、成员管理等接口")
@RestController
@RequestMapping("/family")
@RequiredArgsConstructor
public class FamilyController {

    private final FamilyService familyService;

    @Operation(summary = "创建家庭", description = "创建一个新的家庭并成为家庭创建者")
    @PostMapping("/create")
    public ApiResponse<FamilyResponse> createFamily(
            Authentication authentication,
            @Valid @RequestBody FamilyCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        FamilyResponse response = familyService.createFamily(userDetails.getUsername(), request);
        return ApiResponse.success("家庭创建成功", response);
    }

    @Operation(summary = "加入家庭", description = "通过邀请码加入一个已存在的家庭")
    @PostMapping("/join/{inviteCode}")
    public ApiResponse<FamilyResponse> joinFamily(
            Authentication authentication,
            @PathVariable String inviteCode) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        FamilyResponse response = familyService.joinFamily(userDetails.getUsername(), inviteCode);
        return ApiResponse.success("成功加入家庭", response);
    }

    @Operation(summary = "获取我的家庭列表", description = "获取当前用户所属的所有家庭")
    @GetMapping("/my-families")
    public ApiResponse<List<FamilyResponse>> getMyFamilies(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<FamilyResponse> response = familyService.getUserFamilies(userDetails.getUsername());
        return ApiResponse.success(response);
    }

    @Operation(summary = "获取家庭详情", description = "获取指定家庭的详细信息")
    @GetMapping("/{familyId}")
    public ApiResponse<FamilyResponse> getFamilyDetail(
            Authentication authentication,
            @PathVariable Long familyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        FamilyResponse response = familyService.getFamilyDetail(userDetails.getUsername(), familyId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "添加宝宝", description = "在指定家庭中添加一个新宝宝")
    @PostMapping("/{familyId}/babies")
    public ApiResponse<BabyResponse> addBaby(
            Authentication authentication,
            @PathVariable Long familyId,
            @Valid @RequestBody BabyCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        BabyResponse response = familyService.addBaby(userDetails.getUsername(), familyId, request);
        return ApiResponse.success("宝宝添加成功", response);
    }

    @Operation(summary = "获取家庭宝宝列表", description = "获取指定家庭的所有宝宝")
    @GetMapping("/{familyId}/babies")
    public ApiResponse<List<BabyResponse>> getFamilyBabies(
            Authentication authentication,
            @PathVariable Long familyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<BabyResponse> response = familyService.getFamilyBabies(userDetails.getUsername(), familyId);
        return ApiResponse.success(response);
    }

    // ========== 长辈授权管理（T073） ==========

    @Operation(summary = "更新家庭成员权限", description = "家长/创建者更新家庭成员的昵称、接送确认、通知确认权限（T073）")
    @PutMapping("/{familyId}/members/{memberId}")
    public ApiResponse<FamilyMemberResponse> updateMemberPermissions(
            Authentication authentication,
            @PathVariable Long familyId,
            @PathVariable Long memberId,
            @Valid @RequestBody FamilyMemberUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        FamilyMemberResponse response = familyService.updateMemberPermissions(
                userDetails.getUsername(), familyId, memberId, request);
        return ApiResponse.success("成员权限更新成功", response);
    }
}