package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.AttendanceCheckInRequest;
import com.huigrowth.babycare.dto.AttendanceCheckOutRequest;
import com.huigrowth.babycare.dto.AttendanceResponse;
import com.huigrowth.babycare.dto.AttendanceStatusRequest;
import com.huigrowth.babycare.dto.LeaveRequestCreateRequest;
import com.huigrowth.babycare.dto.LeaveRequestResponse;
import com.huigrowth.babycare.dto.LeaveRequestReviewRequest;
import com.huigrowth.babycare.entity.AttendanceRecord;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.LeaveRequest;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.AttendanceRecordRepository;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.LeaveRequestRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 幼儿考勤与请假服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public AttendanceResponse checkIn(String username, AttendanceCheckInRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, request.getEnrollmentId());
        LocalDate date = resolveDate(request.getAttendanceDate());
        AttendanceRecord record = getOrCreateRecord(enrollment, date);

        record.setStatus(AttendanceRecord.AttendanceStatus.CHECKED_IN);
        record.setCheckInAt(LocalDateTime.now());
        record.setTemperature(request.getTemperature());
        record.setRecordedBy(operator);
        applySourceAndRemark(record, request.getSource(), request.getRemark());

        AttendanceRecord saved = attendanceRecordRepository.save(record);
        log.info("用户 {} 记录宝宝 {} 到园", username, enrollment.getBaby().getName());
        return convertAttendance(saved);
    }

    @Transactional
    public AttendanceResponse checkOut(String username, AttendanceCheckOutRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, request.getEnrollmentId());
        LocalDate date = resolveDate(request.getAttendanceDate());
        AttendanceRecord record = getOrCreateRecord(enrollment, date);

        record.setStatus(AttendanceRecord.AttendanceStatus.CHECKED_OUT);
        record.setCheckOutAt(LocalDateTime.now());
        record.setPickupPersonName(request.getPickupPersonName());
        record.setPickupRelationship(request.getPickupRelationship());
        record.setPickupPhone(request.getPickupPhone());
        record.setRecordedBy(operator);
        applySourceAndRemark(record, request.getSource(), request.getRemark());

        AttendanceRecord saved = attendanceRecordRepository.save(record);
        log.info("用户 {} 记录宝宝 {} 离园", username, enrollment.getBaby().getName());
        return convertAttendance(saved);
    }

    @Transactional
    public AttendanceResponse markAbsent(String username, AttendanceStatusRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, request.getEnrollmentId());
        AttendanceRecord record = getOrCreateRecord(enrollment, resolveDate(request.getAttendanceDate()));

        record.setStatus(AttendanceRecord.AttendanceStatus.ABSENT);
        record.setRemark(request.getRemark());
        record.setRecordedBy(operator);

        return convertAttendance(attendanceRecordRepository.save(record));
    }

    public List<AttendanceResponse> getClassroomAttendance(String username, Long classroomId, LocalDate date) {
        User operator = getUser(username);
        Classroom classroom = getOwnedClassroom(operator, classroomId);

        return attendanceRecordRepository
                .findByEnrollmentClassroomAndAttendanceDateOrderByCreatedAtDesc(classroom, resolveDate(date))
                .stream()
                .map(this::convertAttendance)
                .collect(Collectors.toList());
    }

    public List<AttendanceResponse> getBabyAttendance(
            String username,
            Long babyId,
            LocalDate startDate,
            LocalDate endDate) {
        User operator = getUser(username);
        Baby baby = getAccessibleBabyForAttendance(operator, babyId);
        LocalDate start = startDate != null ? startDate : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? endDate : LocalDate.now();

        if (start.isAfter(end)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }

        return attendanceRecordRepository
                .findByEnrollmentBabyAndAttendanceDateBetweenOrderByAttendanceDateDesc(baby, start, end)
                .stream()
                .map(this::convertAttendance)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestResponse createLeaveRequest(String username, LeaveRequestCreateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getAccessibleEnrollment(operator, request.getEnrollmentId());

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("请假开始日期不能晚于结束日期");
        }

        LeaveRequest leaveRequest = new LeaveRequest();
        leaveRequest.setEnrollment(enrollment);
        leaveRequest.setStartDate(request.getStartDate());
        leaveRequest.setEndDate(request.getEndDate());
        leaveRequest.setType(parseLeaveType(request.getType()));
        leaveRequest.setReason(request.getReason());
        leaveRequest.setRequestedBy(operator);

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("用户 {} 为宝宝 {} 提交请假申请", username, enrollment.getBaby().getName());
        return convertLeave(saved);
    }

    @Transactional
    public LeaveRequestResponse approveLeave(
            String username,
            Long leaveRequestId,
            LeaveRequestReviewRequest request) {
        User operator = getUser(username);
        LeaveRequest leaveRequest = getOwnedLeaveRequest(operator, leaveRequestId);

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.APPROVED);
        leaveRequest.setReviewedBy(operator);
        leaveRequest.setReviewedAt(LocalDateTime.now());
        leaveRequest.setReviewRemark(request.getReviewRemark());
        markLeaveAttendance(leaveRequest, operator);

        return convertLeave(leaveRequestRepository.save(leaveRequest));
    }

    @Transactional
    public LeaveRequestResponse rejectLeave(
            String username,
            Long leaveRequestId,
            LeaveRequestReviewRequest request) {
        User operator = getUser(username);
        LeaveRequest leaveRequest = getOwnedLeaveRequest(operator, leaveRequestId);

        leaveRequest.setStatus(LeaveRequest.LeaveStatus.REJECTED);
        leaveRequest.setReviewedBy(operator);
        leaveRequest.setReviewedAt(LocalDateTime.now());
        leaveRequest.setReviewRemark(request.getReviewRemark());

        return convertLeave(leaveRequestRepository.save(leaveRequest));
    }

    public List<LeaveRequestResponse> getClassroomLeaveRequests(String username, Long classroomId) {
        User operator = getUser(username);
        Classroom classroom = getOwnedClassroom(operator, classroomId);
        return leaveRequestRepository.findByEnrollmentClassroomOrderByCreatedAtDesc(classroom).stream()
                .map(this::convertLeave)
                .collect(Collectors.toList());
    }

    @Transactional
    public LeaveRequestResponse cancelLeaveRequest(String username, Long leaveRequestId, String reason) {
        User operator = getUser(username);
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new BusinessException("请假申请不存在"));
        if (!leaveRequest.getRequestedBy().getId().equals(operator.getId())) {
            throw new BusinessException("您无权取消该请假申请");
        }
        if (leaveRequest.getStatus() != LeaveRequest.LeaveStatus.PENDING) {
            throw new BusinessException("仅待审核的请假申请可以取消");
        }
        leaveRequest.setStatus(LeaveRequest.LeaveStatus.CANCELLED);
        leaveRequest.setReviewRemark(reason);
        log.info("用户 {} 取消请假申请 {}", username, leaveRequestId);
        return convertLeave(leaveRequestRepository.save(leaveRequest));
    }

    public List<LeaveRequestResponse> getMyLeaveRequests(String username) {
        User operator = getUser(username);
        return leaveRequestRepository.findByRequestedByOrderByCreatedAtDesc(operator).stream()
                .map(this::convertLeave)
                .collect(Collectors.toList());
    }

    public List<LeaveRequestResponse> getBabyLeaveRequests(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = getAccessibleBabyForAttendance(operator, babyId);
        return leaveRequestRepository.findByEnrollmentBabyOrderByCreatedAtDesc(baby).stream()
                .map(this::convertLeave)
                .collect(Collectors.toList());
    }

    private void markLeaveAttendance(LeaveRequest leaveRequest, User operator) {
        LocalDate current = leaveRequest.getStartDate();
        while (!current.isAfter(leaveRequest.getEndDate())) {
            AttendanceRecord record = getOrCreateRecord(leaveRequest.getEnrollment(), current);
            record.setStatus(AttendanceRecord.AttendanceStatus.LEAVE);
            record.setRemark(leaveRequest.getReason());
            record.setRecordedBy(operator);
            attendanceRecordRepository.save(record);
            current = current.plusDays(1);
        }
    }

    private AttendanceRecord getOrCreateRecord(Enrollment enrollment, LocalDate date) {
        return attendanceRecordRepository.findByEnrollmentAndAttendanceDate(enrollment, date)
                .orElseGet(() -> {
                    AttendanceRecord record = new AttendanceRecord();
                    record.setEnrollment(enrollment);
                    record.setAttendanceDate(date);
                    return record;
                });
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private Enrollment getOwnedEnrollment(User user, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!canAccessOrganization(user, enrollment.getOrganization().getId())) {
            throw new BusinessException("您无权访问该入托档案");
        }
        return enrollment;
    }

    private Enrollment getAccessibleEnrollment(User user, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!canAccessOrganization(user, enrollment.getOrganization().getId())
                && !canAccessBaby(user, enrollment.getBaby())) {
            throw new BusinessException("您无权访问该入托档案");
        }
        return enrollment;
    }

    private Classroom getOwnedClassroom(User user, Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));
        if (!canAccessOrganization(user, classroom.getOrganization().getId())) {
            throw new BusinessException("您无权访问该班级");
        }
        return classroom;
    }

    private Baby getAccessibleBabyForAttendance(User user, Long babyId) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException("宝宝不存在"));
        if (canAccessBaby(user, baby)) {
            return baby;
        }
        boolean hasOwnedEnrollment = enrollmentRepository.findByBabyOrderByCreatedAtDesc(baby).stream()
                .anyMatch(enrollment -> canAccessOrganization(user, enrollment.getOrganization().getId()));
        if (!hasOwnedEnrollment) {
            throw new BusinessException("您无权访问该宝宝考勤");
        }
        return baby;
    }

    private LeaveRequest getOwnedLeaveRequest(User user, Long leaveRequestId) {
        LeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new BusinessException("请假申请不存在"));
        if (!canAccessOrganization(user, leaveRequest.getEnrollment().getOrganization().getId())) {
            throw new BusinessException("您无权审批该请假申请");
        }
        return leaveRequest;
    }

    private boolean canAccessBaby(User user, Baby baby) {
        return familyMemberRepository.existsByUserAndBaby(user, baby.getFamily().getId());
    }

    private boolean canAccessOrganization(User user, Long organizationId) {
        return organizationRepository.existsByIdAndCreatedBy(organizationId, user);
    }

    private LocalDate resolveDate(LocalDate date) {
        return date != null ? date : LocalDate.now();
    }

    private void applySourceAndRemark(AttendanceRecord record, String source, String remark) {
        if (StringUtils.hasText(source)) {
            record.setSource(source);
        }
        if (remark != null) {
            record.setRemark(remark);
        }
    }

    private LeaveRequest.LeaveType parseLeaveType(String type) {
        if (!StringUtils.hasText(type)) {
            return LeaveRequest.LeaveType.PERSONAL;
        }
        try {
            return LeaveRequest.LeaveType.valueOf(type);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("请假类型不正确");
        }
    }

    private AttendanceResponse convertAttendance(AttendanceRecord record) {
        AttendanceResponse response = new AttendanceResponse();
        response.setId(record.getId());
        response.setEnrollmentId(record.getEnrollment().getId());
        response.setBabyId(record.getEnrollment().getBaby().getId());
        response.setBabyName(record.getEnrollment().getBaby().getName());
        response.setClassroomId(record.getEnrollment().getClassroom().getId());
        response.setClassroomName(record.getEnrollment().getClassroom().getName());
        response.setOrganizationId(record.getEnrollment().getOrganization().getId());
        response.setOrganizationName(record.getEnrollment().getOrganization().getName());
        response.setAttendanceDate(record.getAttendanceDate());
        response.setStatus(record.getStatus());
        response.setStatusDescription(record.getStatus().getDescription());
        response.setCheckInAt(record.getCheckInAt());
        response.setCheckOutAt(record.getCheckOutAt());
        response.setTemperature(record.getTemperature());
        response.setPickupPersonName(record.getPickupPersonName());
        response.setPickupRelationship(record.getPickupRelationship());
        response.setPickupPhone(record.getPickupPhone());
        response.setSource(record.getSource());
        response.setRemark(record.getRemark());
        if (record.getRecordedBy() != null) {
            response.setRecordedById(record.getRecordedBy().getId());
            response.setRecordedByName(record.getRecordedBy().getNickname());
        }
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }

    private LeaveRequestResponse convertLeave(LeaveRequest leaveRequest) {
        LeaveRequestResponse response = new LeaveRequestResponse();
        response.setId(leaveRequest.getId());
        response.setEnrollmentId(leaveRequest.getEnrollment().getId());
        response.setBabyId(leaveRequest.getEnrollment().getBaby().getId());
        response.setBabyName(leaveRequest.getEnrollment().getBaby().getName());
        response.setClassroomId(leaveRequest.getEnrollment().getClassroom().getId());
        response.setClassroomName(leaveRequest.getEnrollment().getClassroom().getName());
        response.setOrganizationId(leaveRequest.getEnrollment().getOrganization().getId());
        response.setOrganizationName(leaveRequest.getEnrollment().getOrganization().getName());
        response.setStartDate(leaveRequest.getStartDate());
        response.setEndDate(leaveRequest.getEndDate());
        response.setType(leaveRequest.getType());
        response.setTypeDescription(leaveRequest.getType().getDescription());
        response.setStatus(leaveRequest.getStatus());
        response.setStatusDescription(leaveRequest.getStatus().getDescription());
        response.setReason(leaveRequest.getReason());
        response.setRequestedById(leaveRequest.getRequestedBy().getId());
        response.setRequestedByName(leaveRequest.getRequestedBy().getNickname());
        if (leaveRequest.getReviewedBy() != null) {
            response.setReviewedById(leaveRequest.getReviewedBy().getId());
            response.setReviewedByName(leaveRequest.getReviewedBy().getNickname());
        }
        response.setReviewedAt(leaveRequest.getReviewedAt());
        response.setReviewRemark(leaveRequest.getReviewRemark());
        response.setCreatedAt(leaveRequest.getCreatedAt());
        response.setUpdatedAt(leaveRequest.getUpdatedAt());
        return response;
    }
}
