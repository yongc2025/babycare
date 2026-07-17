package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.AnnouncementCreateRequest;
import com.huigrowth.babycare.dto.AnnouncementResponse;
import com.huigrowth.babycare.dto.AnnouncementUpdateRequest;
import com.huigrowth.babycare.service.AnnouncementService;
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

@Tag(name = "通知公告", description = "机构/班级通知发布和已读回执接口")
@RestController
@RequestMapping("/announcement")
@RequiredArgsConstructor
public class AnnouncementController {

    private final AnnouncementService announcementService;

    @Operation(summary = "创建通知草稿")
    @PostMapping("/create")
    public ApiResponse<AnnouncementResponse> createAnnouncement(
            Authentication authentication,
            @Valid @RequestBody AnnouncementCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AnnouncementResponse response = announcementService.createAnnouncement(userDetails.getUsername(), request);
        return ApiResponse.success("通知创建成功", response);
    }

    @Operation(summary = "更新通知草稿")
    @PutMapping("/{announcementId}")
    public ApiResponse<AnnouncementResponse> updateAnnouncement(
            Authentication authentication,
            @PathVariable Long announcementId,
            @Valid @RequestBody AnnouncementUpdateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AnnouncementResponse response = announcementService.updateAnnouncement(
                userDetails.getUsername(),
                announcementId,
                request);
        return ApiResponse.success("通知更新成功", response);
    }

    @Operation(summary = "发布通知")
    @PostMapping("/{announcementId}/publish")
    public ApiResponse<AnnouncementResponse> publishAnnouncement(
            Authentication authentication,
            @PathVariable Long announcementId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AnnouncementResponse response = announcementService.publishAnnouncement(
                userDetails.getUsername(),
                announcementId);
        return ApiResponse.success("通知发布成功", response);
    }

    @Operation(summary = "标记已读")
    @PostMapping("/{announcementId}/read")
    public ApiResponse<AnnouncementResponse> markRead(
            Authentication authentication,
            @PathVariable Long announcementId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AnnouncementResponse response = announcementService.markRead(userDetails.getUsername(), announcementId);
        return ApiResponse.success("已标记为已读", response);
    }

    @Operation(summary = "机构通知列表")
    @GetMapping("/organization/{organizationId}")
    public ApiResponse<List<AnnouncementResponse>> getOrganizationAnnouncements(
            Authentication authentication,
            @PathVariable Long organizationId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<AnnouncementResponse> response = announcementService.getOrganizationAnnouncements(
                userDetails.getUsername(),
                organizationId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "班级通知列表")
    @GetMapping("/classroom/{classroomId}")
    public ApiResponse<List<AnnouncementResponse>> getClassroomAnnouncements(
            Authentication authentication,
            @PathVariable Long classroomId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<AnnouncementResponse> response = announcementService.getClassroomAnnouncements(
                userDetails.getUsername(),
                classroomId);
        return ApiResponse.success(response);
    }
}
