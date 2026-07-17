package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.HardwareDeviceRequest;
import com.huigrowth.babycare.dto.HardwareDeviceResponse;
import com.huigrowth.babycare.dto.HardwareEventIngestRequest;
import com.huigrowth.babycare.dto.HardwareEventResponse;
import com.huigrowth.babycare.dto.HardwareEventStatusRequest;
import com.huigrowth.babycare.service.HardwareIntegrationService;
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

@Tag(name = "硬件接入抽象", description = "硬件设备档案和原始设备事件接口")
@RestController
@RequestMapping("/hardware-integration")
@RequiredArgsConstructor
public class HardwareIntegrationController {

    private final HardwareIntegrationService hardwareIntegrationService;

    @Operation(summary = "创建设备档案")
    @PostMapping("/device/create")
    public ApiResponse<HardwareDeviceResponse> createDevice(
            Authentication authentication,
            @Valid @RequestBody HardwareDeviceRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        HardwareDeviceResponse response = hardwareIntegrationService.createDevice(
                userDetails.getUsername(),
                request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "更新设备档案")
    @PutMapping("/device/{deviceId}")
    public ApiResponse<HardwareDeviceResponse> updateDevice(
            Authentication authentication,
            @PathVariable Long deviceId,
            @Valid @RequestBody HardwareDeviceRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        HardwareDeviceResponse response = hardwareIntegrationService.updateDevice(
                userDetails.getUsername(),
                deviceId,
                request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "设备详情")
    @GetMapping("/device/{deviceId}")
    public ApiResponse<HardwareDeviceResponse> getDeviceDetail(
            Authentication authentication,
            @PathVariable Long deviceId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        HardwareDeviceResponse response = hardwareIntegrationService.getDeviceDetail(
                userDetails.getUsername(),
                deviceId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "机构设备列表")
    @GetMapping("/organization/{organizationId}/devices")
    public ApiResponse<List<HardwareDeviceResponse>> getOrganizationDevices(
            Authentication authentication,
            @PathVariable Long organizationId,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String status) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<HardwareDeviceResponse> response = hardwareIntegrationService.getOrganizationDevices(
                userDetails.getUsername(),
                organizationId,
                deviceType,
                status);
        return ApiResponse.success(response);
    }

    @Operation(summary = "接收硬件事件")
    @PostMapping("/event/ingest")
    public ApiResponse<HardwareEventResponse> ingestEvent(
            Authentication authentication,
            @Valid @RequestBody HardwareEventIngestRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        HardwareEventResponse response = hardwareIntegrationService.ingestEvent(
                userDetails.getUsername(),
                request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "标记硬件事件状态")
    @PutMapping("/event/{eventId}/status")
    public ApiResponse<HardwareEventResponse> updateEventStatus(
            Authentication authentication,
            @PathVariable Long eventId,
            @Valid @RequestBody HardwareEventStatusRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        HardwareEventResponse response = hardwareIntegrationService.updateEventStatus(
                userDetails.getUsername(),
                eventId,
                request);
        return ApiResponse.success(response);
    }

    @Operation(summary = "设备事件列表")
    @GetMapping("/device/{deviceId}/events")
    public ApiResponse<List<HardwareEventResponse>> getDeviceEvents(
            Authentication authentication,
            @PathVariable Long deviceId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<HardwareEventResponse> response = hardwareIntegrationService.getDeviceEvents(
                userDetails.getUsername(),
                deviceId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "机构硬件事件列表")
    @GetMapping("/organization/{organizationId}/events")
    public ApiResponse<List<HardwareEventResponse>> getOrganizationEvents(
            Authentication authentication,
            @PathVariable Long organizationId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<HardwareEventResponse> response = hardwareIntegrationService.getOrganizationEvents(
                userDetails.getUsername(),
                organizationId,
                startDate,
                endDate);
        return ApiResponse.success(response);
    }
}
