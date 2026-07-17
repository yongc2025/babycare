package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.HealthObservationCreateRequest;
import com.huigrowth.babycare.dto.HealthObservationResponse;
import com.huigrowth.babycare.dto.HealthObservationUpdateRequest;
import com.huigrowth.babycare.service.HealthObservationService;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@Tag(name = "晨午检与全日观察", description = "晨检、午检、全日观察和异常健康记录接口")
@RestController
@RequestMapping("/health-observation")
@RequiredArgsConstructor
public class HealthObservationController {

    private final HealthObservationService healthObservationService;

    @Operation(summary = "创建健康观察记录")
    @PostMapping("/create")
    public ApiResponse<HealthObservationResponse> createObservation(
            Authentication authentication,
            @Valid @RequestBody HealthObservationCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        HealthObservationResponse response = healthObservationService.createObservation(
                userDetails.getUsername(),
                request);
        return ApiResponse.success("健康观察记录创建成功", response);
    }

    @Operation(summary = "更新健康观察记录")
    @PutMapping("/{observationId}")
    public ApiResponse<HealthObservationResponse> updateObservation(
            Authentication authentication,
            @PathVariable Long observationId,
            @Valid @RequestBody HealthObservationUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        HealthObservationResponse response = healthObservationService.updateObservation(
                userDetails.getUsername(),
                observationId,
                request);
        return ApiResponse.success("健康观察记录更新成功", response);
    }

    @Operation(summary = "删除健康观察记录")
    @DeleteMapping("/{observationId}")
    public ApiResponse<Void> deleteObservation(Authentication authentication, @PathVariable Long observationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        healthObservationService.deleteObservation(userDetails.getUsername(), observationId);
        return ApiResponse.success("健康观察记录删除成功", null);
    }

    @Operation(summary = "班级某日健康观察记录")
    @GetMapping("/classroom/{classroomId}")
    public ApiResponse<List<HealthObservationResponse>> getClassroomObservations(
            Authentication authentication,
            @PathVariable Long classroomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<HealthObservationResponse> response = healthObservationService.getClassroomObservations(
                userDetails.getUsername(),
                classroomId,
                date);
        return ApiResponse.success(response);
    }

    @Operation(summary = "宝宝某日健康观察记录")
    @GetMapping("/baby/{babyId}")
    public ApiResponse<List<HealthObservationResponse>> getBabyObservations(
            Authentication authentication,
            @PathVariable Long babyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<HealthObservationResponse> response = healthObservationService.getBabyObservations(
                userDetails.getUsername(),
                babyId,
                date);
        return ApiResponse.success(response);
    }

    @Operation(summary = "入托档案某日健康观察记录")
    @GetMapping("/enrollment/{enrollmentId}")
    public ApiResponse<List<HealthObservationResponse>> getEnrollmentObservations(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<HealthObservationResponse> response = healthObservationService.getEnrollmentObservations(
                userDetails.getUsername(),
                enrollmentId,
                date);
        return ApiResponse.success(response);
    }
}
