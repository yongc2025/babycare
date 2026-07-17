package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.AttendanceCheckInRequest;
import com.huigrowth.babycare.dto.AttendanceCheckOutRequest;
import com.huigrowth.babycare.dto.AttendanceResponse;
import com.huigrowth.babycare.dto.AttendanceStatusRequest;
import com.huigrowth.babycare.dto.LeaveRequestCreateRequest;
import com.huigrowth.babycare.dto.LeaveRequestResponse;
import com.huigrowth.babycare.dto.LeaveRequestReviewRequest;
import com.huigrowth.babycare.service.AttendanceService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * 幼儿考勤与请假控制器
 */
@Tag(name = "幼儿考勤与请假", description = "到园、离园、缺勤、请假和考勤查询接口")
@RestController
@RequestMapping("/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(summary = "记录到园", description = "为指定入托档案记录宝宝到园")
    @PostMapping("/check-in")
    public ApiResponse<AttendanceResponse> checkIn(
            Authentication authentication,
            @Valid @RequestBody AttendanceCheckInRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AttendanceResponse response = attendanceService.checkIn(userDetails.getUsername(), request);
        return ApiResponse.success("到园记录成功", response);
    }

    @Operation(summary = "记录离园", description = "为指定入托档案记录宝宝离园和接送人快照")
    @PostMapping("/check-out")
    public ApiResponse<AttendanceResponse> checkOut(
            Authentication authentication,
            @Valid @RequestBody AttendanceCheckOutRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AttendanceResponse response = attendanceService.checkOut(userDetails.getUsername(), request);
        return ApiResponse.success("离园记录成功", response);
    }

    @Operation(summary = "记录缺勤", description = "为指定入托档案记录宝宝缺勤")
    @PostMapping("/absent")
    public ApiResponse<AttendanceResponse> markAbsent(
            Authentication authentication,
            @Valid @RequestBody AttendanceStatusRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        AttendanceResponse response = attendanceService.markAbsent(userDetails.getUsername(), request);
        return ApiResponse.success("缺勤记录成功", response);
    }

    @Operation(summary = "班级考勤列表", description = "按班级和日期查询宝宝考勤")
    @GetMapping("/classroom/{classroomId}")
    public ApiResponse<List<AttendanceResponse>> getClassroomAttendance(
            Authentication authentication,
            @PathVariable Long classroomId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<AttendanceResponse> response = attendanceService.getClassroomAttendance(
                userDetails.getUsername(),
                classroomId,
                date);
        return ApiResponse.success(response);
    }

    @Operation(summary = "宝宝考勤记录", description = "按宝宝和日期范围查询考勤记录")
    @GetMapping("/baby/{babyId}")
    public ApiResponse<List<AttendanceResponse>> getBabyAttendance(
            Authentication authentication,
            @PathVariable Long babyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<AttendanceResponse> response = attendanceService.getBabyAttendance(
                userDetails.getUsername(),
                babyId,
                startDate,
                endDate);
        return ApiResponse.success(response);
    }

    @Operation(summary = "创建请假申请", description = "家长或机构人员为宝宝提交请假申请")
    @PostMapping("/leave/request")
    public ApiResponse<LeaveRequestResponse> createLeaveRequest(
            Authentication authentication,
            @Valid @RequestBody LeaveRequestCreateRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        LeaveRequestResponse response = attendanceService.createLeaveRequest(userDetails.getUsername(), request);
        return ApiResponse.success("请假申请提交成功", response);
    }

    @Operation(summary = "审批通过请假", description = "机构管理员审批通过请假申请")
    @PostMapping("/leave/{leaveRequestId}/approve")
    public ApiResponse<LeaveRequestResponse> approveLeave(
            Authentication authentication,
            @PathVariable Long leaveRequestId,
            @Valid @RequestBody LeaveRequestReviewRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        LeaveRequestResponse response = attendanceService.approveLeave(
                userDetails.getUsername(),
                leaveRequestId,
                request);
        return ApiResponse.success("请假审批通过", response);
    }

    @Operation(summary = "审批拒绝请假", description = "机构管理员拒绝请假申请")
    @PostMapping("/leave/{leaveRequestId}/reject")
    public ApiResponse<LeaveRequestResponse> rejectLeave(
            Authentication authentication,
            @PathVariable Long leaveRequestId,
            @Valid @RequestBody LeaveRequestReviewRequest request) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        LeaveRequestResponse response = attendanceService.rejectLeave(
                userDetails.getUsername(),
                leaveRequestId,
                request);
        return ApiResponse.success("请假审批拒绝", response);
    }

    @Operation(summary = "班级请假列表", description = "按班级查询请假申请")
    @GetMapping("/leave/classroom/{classroomId}")
    public ApiResponse<List<LeaveRequestResponse>> getClassroomLeaveRequests(
            Authentication authentication,
            @PathVariable Long classroomId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<LeaveRequestResponse> response = attendanceService.getClassroomLeaveRequests(
                userDetails.getUsername(),
                classroomId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "宝宝请假列表", description = "按宝宝查询请假申请")
    @GetMapping("/leave/baby/{babyId}")
    public ApiResponse<List<LeaveRequestResponse>> getBabyLeaveRequests(
            Authentication authentication,
            @PathVariable Long babyId) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<LeaveRequestResponse> response = attendanceService.getBabyLeaveRequests(
                userDetails.getUsername(),
                babyId);
        return ApiResponse.success(response);
    }
}
