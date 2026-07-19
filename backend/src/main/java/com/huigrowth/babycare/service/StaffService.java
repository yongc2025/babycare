package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.StaffClassroomAssignmentRequest;
import com.huigrowth.babycare.dto.StaffClassroomAssignmentResponse;
import com.huigrowth.babycare.dto.StaffCreateRequest;
import com.huigrowth.babycare.dto.StaffResponse;
import com.huigrowth.babycare.dto.StaffUpdateRequest;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.Staff;
import com.huigrowth.babycare.entity.StaffClassroomAssignment;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.StaffClassroomAssignmentRepository;
import com.huigrowth.babycare.repository.StaffRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 机构员工服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final ClassroomRepository classroomRepository;
    private final StaffClassroomAssignmentRepository staffClassroomAssignmentRepository;

    @Transactional
    public StaffResponse createStaff(String username, StaffCreateRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        User staffUser = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        if (staffRepository.existsByOrganizationAndUser(organization, staffUser)) {
            throw new BusinessException("该用户已经是机构员工");
        }

        Staff staff = new Staff();
        staff.setOrganization(organization);
        staff.setUser(staffUser);
        staff.setRole(parseRole(request.getRole()));

        Staff saved = staffRepository.save(staff);
        log.info("用户 {} 在机构 {} 新增员工: {}", username, organization.getName(), staffUser.getUsername());
        return convertToResponse(saved);
    }

    public List<StaffResponse> getOrganizationStaff(String username, Long organizationId) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, organizationId);
        return staffRepository.findByOrganizationOrderByCreatedAtDesc(organization).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public StaffResponse getStaffDetail(String username, Long staffId) {
        User operator = getUser(username);
        Staff staff = getOwnedStaff(operator, staffId);
        return convertToResponse(staff);
    }

    @Transactional
    public StaffResponse updateStaff(String username, Long staffId, StaffUpdateRequest request) {
        User operator = getUser(username);
        Staff staff = getOwnedStaff(operator, staffId);

        if (StringUtils.hasText(request.getRole())) {
            staff.setRole(parseRole(request.getRole()));
        }
        if (StringUtils.hasText(request.getStatus())) {
            staff.setStatus(parseStatus(request.getStatus()));
        }

        Staff saved = staffRepository.save(staff);
        log.info("用户 {} 更新机构员工: {}", username, saved.getUser().getUsername());
        return convertToResponse(saved);
    }

    // ========== 员工-班级分配 ==========

    @Transactional
    public StaffClassroomAssignmentResponse assignToClassroom(String username, StaffClassroomAssignmentRequest request) {
        User operator = getUser(username);
        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new BusinessException("员工不存在"));
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new BusinessException("班级不存在"));

        // 校验员工和班级属于同一机构
        if (!staff.getOrganization().getId().equals(classroom.getOrganization().getId())) {
            throw new BusinessException("员工和班级不属于同一机构");
        }

        // 校验操作者有权访问该机构
        Organization organization = staff.getOrganization();
        if (!organizationRepository.existsByIdAndCreatedBy(organization.getId(), operator)) {
            throw new BusinessException("您无权操作该机构");
        }

        // 检查是否已存在分配
        if (staffClassroomAssignmentRepository.existsByStaffIdAndClassroomId(staff.getId(), classroom.getId())) {
            throw new BusinessException("该员工已分配到该班级");
        }

        StaffClassroomAssignment assignment = new StaffClassroomAssignment();
        assignment.setStaffId(staff.getId());
        assignment.setClassroomId(classroom.getId());
        if (request.getAssignmentType() != null) {
            assignment.setAssignmentType(StaffClassroomAssignment.AssignmentType.valueOf(request.getAssignmentType()));
        }

        StaffClassroomAssignment saved = staffClassroomAssignmentRepository.save(assignment);
        log.info("用户 {} 将员工 {} 分配到班级 {}", username, staff.getUser().getUsername(), classroom.getName());
        return convertToAssignmentResponse(saved, staff, classroom);
    }

    @Transactional
    public void removeFromClassroom(String username, Long staffId, Long classroomId) {
        User operator = getUser(username);
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));

        Organization organization = staff.getOrganization();
        if (!organizationRepository.existsByIdAndCreatedBy(organization.getId(), operator)) {
            throw new BusinessException("您无权操作该机构");
        }

        staffClassroomAssignmentRepository.deleteByStaffIdAndClassroomId(staffId, classroomId);
        log.info("用户 {} 将员工 {} 从班级 {} 移除", username, staff.getUser().getUsername(), classroom.getName());
    }

    public List<StaffClassroomAssignmentResponse> getClassroomAssignments(String username, Long classroomId) {
        User operator = getUser(username);
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));

        Organization organization = classroom.getOrganization();
        if (!organizationRepository.existsByIdAndCreatedBy(organization.getId(), operator)) {
            throw new BusinessException("您无权访问该机构");
        }

        List<StaffClassroomAssignment> assignments = staffClassroomAssignmentRepository.findByClassroomId(classroomId);
        return assignments.stream().map(a -> {
            Staff staff = staffRepository.findById(a.getStaffId()).orElse(null);
            return convertToAssignmentResponse(a, staff, classroom);
        }).collect(Collectors.toList());
    }

    public List<StaffClassroomAssignmentResponse> getStaffAssignments(String username, Long staffId) {
        User operator = getUser(username);
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new BusinessException("员工不存在"));

        Organization organization = staff.getOrganization();
        if (!organizationRepository.existsByIdAndCreatedBy(organization.getId(), operator)) {
            throw new BusinessException("您无权访问该机构");
        }

        List<StaffClassroomAssignment> assignments = staffClassroomAssignmentRepository.findByStaffId(staffId);
        return assignments.stream().map(a -> {
            Classroom cls = classroomRepository.findById(a.getClassroomId()).orElse(null);
            return convertToAssignmentResponse(a, staff, cls);
        }).collect(Collectors.toList());
    }

    private StaffClassroomAssignmentResponse convertToAssignmentResponse(StaffClassroomAssignment assignment, Staff staff, Classroom classroom) {
        StaffClassroomAssignmentResponse resp = new StaffClassroomAssignmentResponse();
        resp.setId(assignment.getId());
        resp.setStaffId(assignment.getStaffId());
        resp.setStaffName(staff != null ? staff.getUser().getUsername() : "");
        resp.setStaffNickname(staff != null ? staff.getUser().getNickname() : "");
        resp.setStaffRole(staff != null ? staff.getRole().name() : "");
        resp.setClassroomId(assignment.getClassroomId());
        resp.setClassroomName(classroom != null ? classroom.getName() : "");
        resp.setAssignmentType(assignment.getAssignmentType());
        resp.setAssignmentTypeDescription(assignment.getAssignmentType().getDescription());
        resp.setCreatedAt(assignment.getCreatedAt());
        return resp;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private Organization getOwnedOrganization(User user, Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));

        if (!organizationRepository.existsByIdAndCreatedBy(organizationId, user)) {
            throw new BusinessException("您无权访问该机构");
        }

        return organization;
    }

    private Staff getOwnedStaff(User user, Long staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        Long organizationId = staff.getOrganization().getId();

        if (!organizationRepository.existsByIdAndCreatedBy(organizationId, user)) {
            throw new BusinessException("您无权访问该员工");
        }

        return staff;
    }

    private Staff.StaffRole parseRole(String role) {
        try {
            return Staff.StaffRole.valueOf(role);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("员工角色不正确");
        }
    }

    private Staff.StaffStatus parseStatus(String status) {
        try {
            return Staff.StaffStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("员工状态不正确");
        }
    }

    private StaffResponse convertToResponse(Staff staff) {
        StaffResponse response = new StaffResponse();
        response.setId(staff.getId());
        response.setOrganizationId(staff.getOrganization().getId());
        response.setOrganizationName(staff.getOrganization().getName());
        response.setUserId(staff.getUser().getId());
        response.setUsername(staff.getUser().getUsername());
        response.setNickname(staff.getUser().getNickname());
        response.setPhone(staff.getUser().getPhone());
        response.setEmail(staff.getUser().getEmail());
        response.setRole(staff.getRole());
        response.setRoleDescription(staff.getRole().getDescription());
        response.setStatus(staff.getStatus());
        response.setStatusDescription(staff.getStatus().getDescription());
        response.setCreatedAt(staff.getCreatedAt());
        response.setUpdatedAt(staff.getUpdatedAt());
        return response;
    }
}
