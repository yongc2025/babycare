package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.AllergyTagRequest;
import com.huigrowth.babycare.dto.AllergyTagResponse;
import com.huigrowth.babycare.dto.MedicationAdministrationRequest;
import com.huigrowth.babycare.dto.MedicationAdministrationResponse;
import com.huigrowth.babycare.dto.MedicationRequestCreateRequest;
import com.huigrowth.babycare.dto.MedicationRequestResponse;
import com.huigrowth.babycare.dto.MedicationReviewRequest;
import com.huigrowth.babycare.entity.AllergyTag;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.MedicationAdministration;
import com.huigrowth.babycare.entity.MedicationRequest;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.AllergyTagRepository;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.MedicationAdministrationRepository;
import com.huigrowth.babycare.repository.MedicationRequestRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicationCareService {

    private final AllergyTagRepository allergyTagRepository;
    private final MedicationRequestRepository medicationRequestRepository;
    private final MedicationAdministrationRepository administrationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public AllergyTagResponse createAllergyTag(String username, AllergyTagRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getAccessibleEnrollment(operator, request.getEnrollmentId());
        AllergyTag tag = new AllergyTag();
        tag.setEnrollment(enrollment);
        applyAllergyFields(tag, request);
        tag.setCreatedBy(operator);
        return convertAllergy(allergyTagRepository.save(tag));
    }

    @Transactional
    public AllergyTagResponse updateAllergyTag(String username, Long allergyId, AllergyTagRequest request) {
        User operator = getUser(username);
        AllergyTag tag = allergyTagRepository.findById(allergyId)
                .orElseThrow(() -> new BusinessException("过敏标签不存在"));
        if (!canAccessEnrollment(operator, tag.getEnrollment())) {
            throw new BusinessException("您无权操作该过敏标签");
        }
        applyAllergyFields(tag, request);
        return convertAllergy(allergyTagRepository.save(tag));
    }

    public List<AllergyTagResponse> getBabyAllergies(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);
        return allergyTagRepository.findByEnrollmentBabyOrderByCreatedAtDesc(baby).stream()
                .map(this::convertAllergy)
                .collect(Collectors.toList());
    }

    @Transactional
    public MedicationRequestResponse createMedicationRequest(
            String username,
            MedicationRequestCreateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getAccessibleEnrollment(operator, request.getEnrollmentId());
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new BusinessException("用药开始日期不能晚于结束日期");
        }

        MedicationRequest medication = new MedicationRequest();
        medication.setEnrollment(enrollment);
        medication.setMedicineName(request.getMedicineName());
        medication.setDosage(request.getDosage());
        medication.setFrequency(request.getFrequency());
        medication.setStartDate(request.getStartDate());
        medication.setEndDate(request.getEndDate());
        medication.setInstructions(request.getInstructions());
        medication.setRequestedBy(operator);

        return convertMedication(medicationRequestRepository.save(medication));
    }

    @Transactional
    public MedicationRequestResponse approveMedication(
            String username,
            Long medicationRequestId,
            MedicationReviewRequest request) {
        User operator = getUser(username);
        MedicationRequest medication = getOwnedMedication(medicationRequestId, operator);
        medication.setStatus(MedicationRequest.MedicationStatus.APPROVED);
        medication.setReviewedBy(operator);
        medication.setReviewedAt(LocalDateTime.now());
        medication.setReviewRemark(request.getReviewRemark());
        return convertMedication(medicationRequestRepository.save(medication));
    }

    @Transactional
    public MedicationRequestResponse rejectMedication(
            String username,
            Long medicationRequestId,
            MedicationReviewRequest request) {
        User operator = getUser(username);
        MedicationRequest medication = getOwnedMedication(medicationRequestId, operator);
        medication.setStatus(MedicationRequest.MedicationStatus.REJECTED);
        medication.setReviewedBy(operator);
        medication.setReviewedAt(LocalDateTime.now());
        medication.setReviewRemark(request.getReviewRemark());
        return convertMedication(medicationRequestRepository.save(medication));
    }

    @Transactional
    public MedicationAdministrationResponse recordAdministration(
            String username,
            MedicationAdministrationRequest request) {
        User operator = getUser(username);
        MedicationRequest medication = getOwnedMedication(request.getMedicationRequestId(), operator);
        if (medication.getStatus() != MedicationRequest.MedicationStatus.APPROVED) {
            throw new BusinessException("用药委托未审核通过，不能执行用药");
        }

        MedicationAdministration administration = new MedicationAdministration();
        administration.setMedicationRequest(medication);
        administration.setAdministeredAt(request.getAdministeredAt() != null
                ? request.getAdministeredAt()
                : LocalDateTime.now());
        administration.setActualDosage(request.getActualDosage());
        administration.setReactionObserved(Boolean.TRUE.equals(request.getReactionObserved()));
        administration.setRemark(request.getRemark());
        administration.setAdministeredBy(operator);

        return convertAdministration(administrationRepository.save(administration));
    }

    public List<MedicationRequestResponse> getBabyMedications(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);
        return medicationRequestRepository.findByEnrollmentBabyOrderByCreatedAtDesc(baby).stream()
                .map(this::convertMedication)
                .collect(Collectors.toList());
    }

    public List<MedicationRequestResponse> getClassroomMedications(String username, Long classroomId) {
        User operator = getUser(username);
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));
        if (!canAccessOrganization(operator, classroom.getOrganization().getId())) {
            throw new BusinessException("您无权访问该班级");
        }
        return medicationRequestRepository.findByEnrollmentClassroomOrderByCreatedAtDesc(classroom).stream()
                .map(this::convertMedication)
                .collect(Collectors.toList());
    }

    private MedicationRequest getOwnedMedication(Long medicationRequestId, User operator) {
        MedicationRequest medication = medicationRequestRepository.findById(medicationRequestId)
                .orElseThrow(() -> new BusinessException("用药委托不存在"));
        if (!canAccessOrganization(operator, medication.getEnrollment().getOrganization().getId())) {
            throw new BusinessException("您无权操作该用药委托");
        }
        return medication;
    }

    private Enrollment getAccessibleEnrollment(User user, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!canAccessEnrollment(user, enrollment)) {
            throw new BusinessException("您无权访问该入托档案");
        }
        return enrollment;
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
            throw new BusinessException("您无权访问该宝宝用药过敏信息");
        }
        return baby;
    }

    private boolean canAccessEnrollment(User user, Enrollment enrollment) {
        return canAccessOrganization(user, enrollment.getOrganization().getId())
                || canAccessBaby(user, enrollment.getBaby());
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

    private void applyAllergyFields(AllergyTag tag, AllergyTagRequest request) {
        tag.setAllergen(request.getAllergen());
        tag.setReaction(request.getReaction());
        tag.setSeverity(parseSeverity(request.getSeverity()));
        if (request.getStatus() != null) {
            tag.setStatus(parseAllergyStatus(request.getStatus()));
        }
        tag.setRemark(request.getRemark());
    }

    private AllergyTag.AllergySeverity parseSeverity(String severity) {
        if (severity == null) {
            return AllergyTag.AllergySeverity.MILD;
        }
        try {
            return AllergyTag.AllergySeverity.valueOf(severity);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("过敏严重程度不正确");
        }
    }

    private AllergyTag.AllergyStatus parseAllergyStatus(String status) {
        try {
            return AllergyTag.AllergyStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("过敏标签状态不正确");
        }
    }

    private AllergyTagResponse convertAllergy(AllergyTag tag) {
        AllergyTagResponse response = new AllergyTagResponse();
        response.setId(tag.getId());
        response.setEnrollmentId(tag.getEnrollment().getId());
        response.setBabyId(tag.getEnrollment().getBaby().getId());
        response.setBabyName(tag.getEnrollment().getBaby().getName());
        response.setAllergen(tag.getAllergen());
        response.setReaction(tag.getReaction());
        response.setSeverity(tag.getSeverity());
        response.setSeverityDescription(tag.getSeverity().getDescription());
        response.setStatus(tag.getStatus());
        response.setStatusDescription(tag.getStatus().getDescription());
        response.setRemark(tag.getRemark());
        if (tag.getCreatedBy() != null) {
            response.setCreatedById(tag.getCreatedBy().getId());
            response.setCreatedByName(tag.getCreatedBy().getNickname());
        }
        response.setCreatedAt(tag.getCreatedAt());
        response.setUpdatedAt(tag.getUpdatedAt());
        return response;
    }

    private MedicationRequestResponse convertMedication(MedicationRequest medication) {
        MedicationRequestResponse response = new MedicationRequestResponse();
        response.setId(medication.getId());
        response.setEnrollmentId(medication.getEnrollment().getId());
        response.setBabyId(medication.getEnrollment().getBaby().getId());
        response.setBabyName(medication.getEnrollment().getBaby().getName());
        response.setClassroomId(medication.getEnrollment().getClassroom().getId());
        response.setClassroomName(medication.getEnrollment().getClassroom().getName());
        response.setOrganizationId(medication.getEnrollment().getOrganization().getId());
        response.setOrganizationName(medication.getEnrollment().getOrganization().getName());
        response.setMedicineName(medication.getMedicineName());
        response.setDosage(medication.getDosage());
        response.setFrequency(medication.getFrequency());
        response.setStartDate(medication.getStartDate());
        response.setEndDate(medication.getEndDate());
        response.setInstructions(medication.getInstructions());
        response.setStatus(medication.getStatus());
        response.setStatusDescription(medication.getStatus().getDescription());
        response.setRequestedById(medication.getRequestedBy().getId());
        response.setRequestedByName(medication.getRequestedBy().getNickname());
        if (medication.getReviewedBy() != null) {
            response.setReviewedById(medication.getReviewedBy().getId());
            response.setReviewedByName(medication.getReviewedBy().getNickname());
        }
        response.setReviewedAt(medication.getReviewedAt());
        response.setReviewRemark(medication.getReviewRemark());
        response.setAdministrations(administrationRepository
                .findByMedicationRequestOrderByAdministeredAtDesc(medication)
                .stream()
                .map(this::convertAdministration)
                .collect(Collectors.toList()));
        response.setCreatedAt(medication.getCreatedAt());
        response.setUpdatedAt(medication.getUpdatedAt());
        return response;
    }

    private MedicationAdministrationResponse convertAdministration(MedicationAdministration administration) {
        MedicationAdministrationResponse response = new MedicationAdministrationResponse();
        response.setId(administration.getId());
        response.setMedicationRequestId(administration.getMedicationRequest().getId());
        response.setAdministeredAt(administration.getAdministeredAt());
        response.setActualDosage(administration.getActualDosage());
        response.setReactionObserved(administration.getReactionObserved());
        response.setRemark(administration.getRemark());
        response.setAdministeredById(administration.getAdministeredBy().getId());
        response.setAdministeredByName(administration.getAdministeredBy().getNickname());
        response.setCreatedAt(administration.getCreatedAt());
        response.setUpdatedAt(administration.getUpdatedAt());
        return response;
    }
}
