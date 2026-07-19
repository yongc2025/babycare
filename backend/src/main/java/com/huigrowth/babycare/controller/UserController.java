package com.huigrowth.babycare.controller;

import com.huigrowth.babycare.dto.UserStaffInfoResponse;
import com.huigrowth.babycare.entity.Staff;
import com.huigrowth.babycare.entity.StaffClassroomAssignment;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.*;
import com.huigrowth.babycare.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息控制器
 */
@Tag(name = "用户信息", description = "当前用户信息、岗位和授权查询")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final StaffClassroomAssignmentRepository staffClassroomAssignmentRepository;
    private final ClassroomRepository classroomRepository;

    @Operation(summary = "获取当前用户的岗位与班级授权信息", description = "返回用户的 Staff 记录和分配的班级列表，用于前端权限控制")
    @GetMapping("/my-staff-info")
    public ApiResponse<UserStaffInfoResponse> getMyStaffInfo(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        List<Staff> staffRecords = staffRepository.findByUserOrderByCreatedAtDesc(user);

        UserStaffInfoResponse resp = new UserStaffInfoResponse();
        List<UserStaffInfoResponse.StaffInfo> staffInfos = new ArrayList<>();

        for (Staff s : staffRecords) {
            UserStaffInfoResponse.StaffInfo info = new UserStaffInfoResponse.StaffInfo();
            info.setStaffId(s.getId());
            info.setOrganizationId(s.getOrganization().getId());
            info.setOrganizationName(s.getOrganization().getName());
            info.setRole(s.getRole().name());
            info.setRoleDescription(s.getRole().getDescription());

            // 获取该员工分配的班级
            List<StaffClassroomAssignment> assignments = staffClassroomAssignmentRepository.findByStaffId(s.getId());
            List<UserStaffInfoResponse.ClassroomInfo> classrooms = assignments.stream().map(a -> {
                UserStaffInfoResponse.ClassroomInfo ci = new UserStaffInfoResponse.ClassroomInfo();
                ci.setClassroomId(a.getClassroomId());
                ci.setAssignmentType(a.getAssignmentType().name());
                ci.setAssignmentTypeDescription(a.getAssignmentType().getDescription());
                classroomRepository.findById(a.getClassroomId()).ifPresent(c -> ci.setClassroomName(c.getName()));
                return ci;
            }).collect(Collectors.toList());
            info.setAssignedClassrooms(classrooms);
            staffInfos.add(info);
        }

        resp.setStaffInfos(staffInfos);
        return ApiResponse.success(resp);
    }
}
