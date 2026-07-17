package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.CareRecordCreateRequest;
import com.huigrowth.babycare.dto.CareRecordResponse;
import com.huigrowth.babycare.dto.CareRecordUpdateRequest;
import com.huigrowth.babycare.service.CareRecordService;
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

@Tag(name = "一日照护记录", description = "喂养、饮水、睡眠、如厕、体温、情绪、活动记录接口")
@RestController
@RequestMapping("/care-record")
@RequiredArgsConstructor
public class CareRecordController {

    private final CareRecordService careRecordService;

    @Operation(summary = "创建照护记录")
    @PostMapping("/create")
    public ApiResponse<CareRecordResponse> createRecord(
            Authentication authentication,
            @Valid @RequestBody CareRecordCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CareRecordResponse response = careRecordService.createRecord(userDetails.getUsername(), request);
        return ApiResponse.success("照护记录创建成功", response);
    }

    @Operation(summary = "更新照护记录")
    @PutMapping("/{recordId}")
    public ApiResponse<CareRecordResponse> updateRecord(
            Authentication authentication,
            @PathVariable Long recordId,
            @Valid @RequestBody CareRecordUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        CareRecordResponse response = careRecordService.updateRecord(
                userDetails.getUsername(),
                recordId,
                request);
        return ApiResponse.success("照护记录更新成功", response);
    }

    @Operation(summary = "删除照护记录")
    @DeleteMapping("/{recordId}")
    public ApiResponse<Void> deleteRecord(Authentication authentication, @PathVariable Long recordId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        careRecordService.deleteRecord(userDetails.getUsername(), recordId);
        return ApiResponse.success("照护记录删除成功", null);
    }

    @Operation(summary = "班级某日照护记录")
    @GetMapping("/classroom/{classroomId}")
    public ApiResponse<List<CareRecordResponse>> getClassroomRecords(
            Authentication authentication,
            @PathVariable Long classroomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<CareRecordResponse> response = careRecordService.getClassroomRecords(
                userDetails.getUsername(),
                classroomId,
                date);
        return ApiResponse.success(response);
    }

    @Operation(summary = "宝宝某日照护记录")
    @GetMapping("/baby/{babyId}")
    public ApiResponse<List<CareRecordResponse>> getBabyRecords(
            Authentication authentication,
            @PathVariable Long babyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<CareRecordResponse> response = careRecordService.getBabyRecords(
                userDetails.getUsername(),
                babyId,
                date);
        return ApiResponse.success(response);
    }

    @Operation(summary = "入托档案某日照护记录")
    @GetMapping("/enrollment/{enrollmentId}")
    public ApiResponse<List<CareRecordResponse>> getEnrollmentRecords(
            Authentication authentication,
            @PathVariable Long enrollmentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<CareRecordResponse> response = careRecordService.getEnrollmentRecords(
                userDetails.getUsername(),
                enrollmentId,
                date);
        return ApiResponse.success(response);
    }
}
