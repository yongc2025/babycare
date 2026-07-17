package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.MealIntakeRequest;
import com.huigrowth.babycare.dto.MealIntakeResponse;
import com.huigrowth.babycare.dto.MealPlanRequest;
import com.huigrowth.babycare.dto.MealPlanResponse;
import com.huigrowth.babycare.service.MealPlanService;
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

@Tag(name = "食谱与膳食记录", description = "食谱维护、公示和宝宝实际进食记录接口")
@RestController
@RequestMapping("/meal-plan")
@RequiredArgsConstructor
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @Operation(summary = "创建食谱")
    @PostMapping("/create")
    public ApiResponse<MealPlanResponse> createMealPlan(
            Authentication authentication,
            @Valid @RequestBody MealPlanRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        MealPlanResponse response = mealPlanService.createMealPlan(userDetails.getUsername(), request);
        return ApiResponse.success("食谱创建成功", response);
    }

    @Operation(summary = "更新食谱")
    @PutMapping("/{mealPlanId}")
    public ApiResponse<MealPlanResponse> updateMealPlan(
            Authentication authentication,
            @PathVariable Long mealPlanId,
            @Valid @RequestBody MealPlanRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        MealPlanResponse response = mealPlanService.updateMealPlan(
                userDetails.getUsername(),
                mealPlanId,
                request);
        return ApiResponse.success("食谱更新成功", response);
    }

    @Operation(summary = "发布食谱")
    @PostMapping("/{mealPlanId}/publish")
    public ApiResponse<MealPlanResponse> publishMealPlan(
            Authentication authentication,
            @PathVariable Long mealPlanId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        MealPlanResponse response = mealPlanService.publishMealPlan(userDetails.getUsername(), mealPlanId);
        return ApiResponse.success("食谱发布成功", response);
    }

    @Operation(summary = "机构食谱列表")
    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<MealPlanResponse>> getOrganizationMeals(
            Authentication authentication,
            @PathVariable Long organizationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<MealPlanResponse> response = mealPlanService.getOrganizationMeals(
                userDetails.getUsername(),
                organizationId,
                date,
                startDate,
                endDate);
        return ApiResponse.success(response);
    }

    @Operation(summary = "记录宝宝实际进食")
    @PostMapping("/intake/record")
    public ApiResponse<MealIntakeResponse> recordIntake(
            Authentication authentication,
            @Valid @RequestBody MealIntakeRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        MealIntakeResponse response = mealPlanService.recordIntake(userDetails.getUsername(), request);
        return ApiResponse.success("进食记录保存成功", response);
    }

    @Operation(summary = "食谱进食记录")
    @GetMapping("/{mealPlanId}/intakes")
    public ApiResponse<List<MealIntakeResponse>> getMealIntakes(
            Authentication authentication,
            @PathVariable Long mealPlanId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<MealIntakeResponse> response = mealPlanService.getMealIntakes(userDetails.getUsername(), mealPlanId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "宝宝进食记录")
    @GetMapping("/intake/enrollment/{enrollmentId}")
    public ApiResponse<List<MealIntakeResponse>> getEnrollmentIntakes(
            Authentication authentication,
            @PathVariable Long enrollmentId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<MealIntakeResponse> response = mealPlanService.getEnrollmentIntakes(
                userDetails.getUsername(),
                enrollmentId);
        return ApiResponse.success(response);
    }
}
