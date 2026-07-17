package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.IncidentReportCreateRequest;
import com.huigrowth.babycare.dto.IncidentReportResponse;
import com.huigrowth.babycare.dto.IncidentReportUpdateRequest;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.IncidentReport;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.IncidentReportRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidentReportService {

    private final IncidentReportRepository incidentReportRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public IncidentReportResponse createReport(String username, IncidentReportCreateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, request.getEnrollmentId());

        IncidentReport report = new IncidentReport();
        report.setEnrollment(enrollment);
        report.setType(parseType(request.getType()));
        report.setSeverity(parseSeverity(request.getSeverity()));
        report.setOccurredAt(request.getOccurredAt() != null ? request.getOccurredAt() : LocalDateTime.now());
        report.setLocation(request.getLocation());
        report.setTitle(request.getTitle());
        report.setDescription(request.getDescription());
        report.setHandlingProcess(request.getHandlingProcess());
        report.setFollowUpPlan(request.getFollowUpPlan());
        report.setReportedBy(operator);

        return convert(incidentReportRepository.save(report));
    }

    @Transactional
    public IncidentReportResponse updateReport(String username, Long reportId, IncidentReportUpdateRequest request) {
        User operator = getUser(username);
        IncidentReport report = getOwnedReport(operator, reportId);
        applyUpdates(report, request, operator);
        return convert(incidentReportRepository.save(report));
    }

    @Transactional
    public IncidentReportResponse closeReport(String username, Long reportId) {
        User operator = getUser(username);
        IncidentReport report = getOwnedReport(operator, reportId);
        report.setStatus(IncidentReport.IncidentStatus.CLOSED);
        report.setHandledBy(operator);
        return convert(incidentReportRepository.save(report));
    }

    @Transactional
    public IncidentReportResponse confirmByParent(String username, Long reportId) {
        User operator = getUser(username);
        IncidentReport report = incidentReportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException("异常事故记录不存在"));
        if (!canAccessBaby(operator, report.getEnrollment().getBaby())) {
            throw new BusinessException("您无权确认该异常事故记录");
        }
        report.setParentConfirmed(true);
        report.setParentConfirmedAt(LocalDateTime.now());
        report.setConfirmedBy(operator);
        return convert(incidentReportRepository.save(report));
    }

    public List<IncidentReportResponse> getBabyReports(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);
        return incidentReportRepository.findByEnrollmentBabyOrderByOccurredAtDesc(baby).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public List<IncidentReportResponse> getClassroomReports(String username, Long classroomId, String status) {
        User operator = getUser(username);
        Classroom classroom = getOwnedClassroom(operator, classroomId);
        if (StringUtils.hasText(status)) {
            return incidentReportRepository
                    .findByEnrollmentClassroomAndStatusOrderByOccurredAtDesc(classroom, parseStatus(status))
                    .stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
        }
        return incidentReportRepository.findByEnrollmentClassroomOrderByOccurredAtDesc(classroom).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private void applyUpdates(IncidentReport report, IncidentReportUpdateRequest request, User operator) {
        if (StringUtils.hasText(request.getType())) {
            report.setType(parseType(request.getType()));
        }
        if (StringUtils.hasText(request.getSeverity())) {
            report.setSeverity(parseSeverity(request.getSeverity()));
        }
        if (StringUtils.hasText(request.getStatus())) {
            report.setStatus(parseStatus(request.getStatus()));
        }
        if (request.getOccurredAt() != null) {
            report.setOccurredAt(request.getOccurredAt());
        }
        if (request.getLocation() != null) {
            report.setLocation(request.getLocation());
        }
        if (request.getTitle() != null) {
            report.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            report.setDescription(request.getDescription());
        }
        if (request.getHandlingProcess() != null) {
            report.setHandlingProcess(request.getHandlingProcess());
            report.setHandledBy(operator);
            if (report.getStatus() == IncidentReport.IncidentStatus.OPEN) {
                report.setStatus(IncidentReport.IncidentStatus.PROCESSING);
            }
        }
        if (request.getFollowUpPlan() != null) {
            report.setFollowUpPlan(request.getFollowUpPlan());
        }
        if (Boolean.TRUE.equals(request.getParentNotified())) {
            report.setParentNotified(true);
            report.setParentNotifiedAt(LocalDateTime.now());
        }
    }

    private IncidentReport getOwnedReport(User user, Long reportId) {
        IncidentReport report = incidentReportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException("异常事故记录不存在"));
        if (!canAccessOrganization(user, report.getEnrollment().getOrganization().getId())) {
            throw new BusinessException("您无权操作该异常事故记录");
        }
        return report;
    }

    private Enrollment getOwnedEnrollment(User user, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!canAccessOrganization(user, enrollment.getOrganization().getId())) {
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

    private Baby getAccessibleBaby(User user, Long babyId) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException("宝宝不存在"));
        if (canAccessBaby(user, baby)) {
            return baby;
        }
        boolean hasOwnedEnrollment = enrollmentRepository.findByBabyOrderByCreatedAtDesc(baby).stream()
                .anyMatch(enrollment -> canAccessOrganization(user, enrollment.getOrganization().getId()));
        if (!hasOwnedEnrollment) {
            throw new BusinessException("您无权访问该宝宝异常事故记录");
        }
        return baby;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private boolean canAccessBaby(User user, Baby baby) {
        return familyMemberRepository.existsByUserAndBaby(user, baby.getFamily().getId());
    }

    private boolean canAccessOrganization(User user, Long organizationId) {
        return organizationRepository.existsByIdAndCreatedBy(organizationId, user);
    }

    private IncidentReport.IncidentType parseType(String type) {
        try {
            return IncidentReport.IncidentType.valueOf(type);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("异常事故类型不正确");
        }
    }

    private IncidentReport.IncidentSeverity parseSeverity(String severity) {
        if (!StringUtils.hasText(severity)) {
            return IncidentReport.IncidentSeverity.LOW;
        }
        try {
            return IncidentReport.IncidentSeverity.valueOf(severity);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("异常事故严重程度不正确");
        }
    }

    private IncidentReport.IncidentStatus parseStatus(String status) {
        try {
            return IncidentReport.IncidentStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("异常事故状态不正确");
        }
    }

    private IncidentReportResponse convert(IncidentReport report) {
        IncidentReportResponse response = new IncidentReportResponse();
        response.setId(report.getId());
        response.setEnrollmentId(report.getEnrollment().getId());
        response.setBabyId(report.getEnrollment().getBaby().getId());
        response.setBabyName(report.getEnrollment().getBaby().getName());
        response.setClassroomId(report.getEnrollment().getClassroom().getId());
        response.setClassroomName(report.getEnrollment().getClassroom().getName());
        response.setOrganizationId(report.getEnrollment().getOrganization().getId());
        response.setOrganizationName(report.getEnrollment().getOrganization().getName());
        response.setType(report.getType());
        response.setTypeDescription(report.getType().getDescription());
        response.setSeverity(report.getSeverity());
        response.setSeverityDescription(report.getSeverity().getDescription());
        response.setStatus(report.getStatus());
        response.setStatusDescription(report.getStatus().getDescription());
        response.setOccurredAt(report.getOccurredAt());
        response.setLocation(report.getLocation());
        response.setTitle(report.getTitle());
        response.setDescription(report.getDescription());
        response.setHandlingProcess(report.getHandlingProcess());
        response.setFollowUpPlan(report.getFollowUpPlan());
        response.setParentNotified(report.getParentNotified());
        response.setParentNotifiedAt(report.getParentNotifiedAt());
        response.setParentConfirmed(report.getParentConfirmed());
        response.setParentConfirmedAt(report.getParentConfirmedAt());
        response.setReportedById(report.getReportedBy().getId());
        response.setReportedByName(report.getReportedBy().getNickname());
        if (report.getHandledBy() != null) {
            response.setHandledById(report.getHandledBy().getId());
            response.setHandledByName(report.getHandledBy().getNickname());
        }
        if (report.getConfirmedBy() != null) {
            response.setConfirmedById(report.getConfirmedBy().getId());
            response.setConfirmedByName(report.getConfirmedBy().getNickname());
        }
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        return response;
    }
}
