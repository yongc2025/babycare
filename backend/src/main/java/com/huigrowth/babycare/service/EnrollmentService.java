package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.EnrollmentCreateRequest;
import com.huigrowth.babycare.dto.EnrollmentResponse;
import com.huigrowth.babycare.dto.EnrollmentUpdateRequest;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 宝宝入托档案服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public EnrollmentResponse createEnrollment(String username, EnrollmentCreateRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        Classroom classroom = getClassroomInOrganization(request.getClassroomId(), organization);
        Baby baby = getAccessibleBaby(operator, request.getBabyId());

        boolean hasActiveEnrollment = enrollmentRepository.existsByBabyAndOrganizationAndStatusIn(
                baby,
                organization,
                List.of(Enrollment.EnrollmentStatus.PENDING, Enrollment.EnrollmentStatus.ACTIVE));
        if (hasActiveEnrollment) {
            throw new BusinessException("该宝宝已存在有效入托档案");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setBaby(baby);
        enrollment.setOrganization(organization);
        enrollment.setClassroom(classroom);
        enrollment.setStatus(Enrollment.EnrollmentStatus.ACTIVE);
        enrollment.setEnrolledAt(request.getEnrolledAt());
        enrollment.setAllergyNotes(request.getAllergyNotes());
        enrollment.setMedicalNotes(request.getMedicalNotes());
        enrollment.setSpecialCareNotes(request.getSpecialCareNotes());
        enrollment.setEmergencyContactName(request.getEmergencyContactName());
        enrollment.setEmergencyContactPhone(request.getEmergencyContactPhone());

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("用户 {} 为宝宝 {} 创建入托档案", username, baby.getName());
        return convertToResponse(saved);
    }

    public List<EnrollmentResponse> getClassroomEnrollments(String username, Long classroomId) {
        User operator = getUser(username);
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));
        getOwnedOrganization(operator, classroom.getOrganization().getId());

        return enrollmentRepository.findByClassroomOrderByCreatedAtDesc(classroom).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponse> getBabyEnrollments(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);

        return enrollmentRepository.findByBabyOrderByCreatedAtDesc(baby).stream()
                .filter(enrollment -> canAccessOrganization(operator, enrollment.getOrganization().getId())
                        || canAccessBaby(operator, enrollment.getBaby()))
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public EnrollmentResponse getEnrollmentDetail(String username, Long enrollmentId) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);
        return convertToResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse updateEnrollment(String username, Long enrollmentId, EnrollmentUpdateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, enrollmentId);

        if (request.getClassroomId() != null) {
            Classroom classroom = getClassroomInOrganization(
                    request.getClassroomId(),
                    enrollment.getOrganization());
            enrollment.setClassroom(classroom);
        }
        if (StringUtils.hasText(request.getStatus())) {
            enrollment.setStatus(parseStatus(request.getStatus()));
        }
        if (request.getEnrolledAt() != null) {
            enrollment.setEnrolledAt(request.getEnrolledAt());
        }
        if (request.getAllergyNotes() != null) {
            enrollment.setAllergyNotes(request.getAllergyNotes());
        }
        if (request.getMedicalNotes() != null) {
            enrollment.setMedicalNotes(request.getMedicalNotes());
        }
        if (request.getSpecialCareNotes() != null) {
            enrollment.setSpecialCareNotes(request.getSpecialCareNotes());
        }
        if (request.getEmergencyContactName() != null) {
            enrollment.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            enrollment.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }

        Enrollment saved = enrollmentRepository.save(enrollment);
        log.info("用户 {} 更新入托档案: {}", username, saved.getId());
        return convertToResponse(saved);
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

    private Classroom getClassroomInOrganization(Long classroomId, Organization organization) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));

        if (!classroom.getOrganization().getId().equals(organization.getId())) {
            throw new BusinessException("班级不属于该机构");
        }

        return classroom;
    }

    private Baby getAccessibleBaby(User user, Long babyId) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException("宝宝不存在"));

        if (!canAccessBaby(user, baby)) {
            throw new BusinessException("您无权访问该宝宝");
        }

        return baby;
    }

    private Enrollment getOwnedEnrollment(User user, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));

        if (!canAccessOrganization(user, enrollment.getOrganization().getId())) {
            throw new BusinessException("您无权访问该入托档案");
        }

        return enrollment;
    }

    private boolean canAccessBaby(User user, Baby baby) {
        return familyMemberRepository.existsByUserAndBaby(user, baby.getFamily().getId());
    }

    private boolean canAccessOrganization(User user, Long organizationId) {
        return organizationRepository.existsByIdAndCreatedBy(organizationId, user);
    }

    private Enrollment.EnrollmentStatus parseStatus(String status) {
        try {
            return Enrollment.EnrollmentStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("入托状态不正确");
        }
    }

    private EnrollmentResponse convertToResponse(Enrollment enrollment) {
        EnrollmentResponse response = new EnrollmentResponse();
        response.setId(enrollment.getId());
        response.setBabyId(enrollment.getBaby().getId());
        response.setBabyName(enrollment.getBaby().getName());
        response.setBabyGender(enrollment.getBaby().getGender());
        response.setBabyBirthday(enrollment.getBaby().getBirthday());
        response.setFamilyId(enrollment.getBaby().getFamily().getId());
        response.setOrganizationId(enrollment.getOrganization().getId());
        response.setOrganizationName(enrollment.getOrganization().getName());
        response.setClassroomId(enrollment.getClassroom().getId());
        response.setClassroomName(enrollment.getClassroom().getName());
        response.setStatus(enrollment.getStatus());
        response.setStatusDescription(enrollment.getStatus().getDescription());
        response.setEnrolledAt(enrollment.getEnrolledAt());
        response.setAllergyNotes(enrollment.getAllergyNotes());
        response.setMedicalNotes(enrollment.getMedicalNotes());
        response.setSpecialCareNotes(enrollment.getSpecialCareNotes());
        response.setEmergencyContactName(enrollment.getEmergencyContactName());
        response.setEmergencyContactPhone(enrollment.getEmergencyContactPhone());
        response.setCreatedAt(enrollment.getCreatedAt());
        response.setUpdatedAt(enrollment.getUpdatedAt());
        return response;
    }
}
