package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.PickupDelegationCreateRequest;
import com.huigrowth.babycare.dto.PickupDelegationResponse;
import com.huigrowth.babycare.dto.PickupDelegationReviewRequest;
import com.huigrowth.babycare.dto.PickupPersonCreateRequest;
import com.huigrowth.babycare.dto.PickupPersonResponse;
import com.huigrowth.babycare.dto.PickupPersonUpdateRequest;
import com.huigrowth.babycare.entity.AuthorizedPickupPerson;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.PickupDelegation;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.AuthorizedPickupPersonRepository;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.PickupDelegationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PickupService {

    private final AuthorizedPickupPersonRepository pickupPersonRepository;
    private final PickupDelegationRepository pickupDelegationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public PickupPersonResponse createPickupPerson(String username, PickupPersonCreateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getAccessibleEnrollment(operator, request.getEnrollmentId());

        AuthorizedPickupPerson person = new AuthorizedPickupPerson();
        person.setEnrollment(enrollment);
        person.setName(request.getName());
        person.setRelationship(request.getRelationship());
        person.setPhone(request.getPhone());
        person.setIdentityNo(request.getIdentityNo());
        person.setPhotoUrl(request.getPhotoUrl());
        person.setRemark(request.getRemark());
        person.setCreatedBy(operator);

        return convertPerson(pickupPersonRepository.save(person));
    }

    @Transactional
    public PickupPersonResponse updatePickupPerson(String username, Long personId, PickupPersonUpdateRequest request) {
        User operator = getUser(username);
        AuthorizedPickupPerson person = getAccessiblePickupPerson(operator, personId);

        if (StringUtils.hasText(request.getName())) {
            person.setName(request.getName());
        }
        if (request.getRelationship() != null) {
            person.setRelationship(request.getRelationship());
        }
        if (request.getPhone() != null) {
            person.setPhone(request.getPhone());
        }
        if (request.getIdentityNo() != null) {
            person.setIdentityNo(request.getIdentityNo());
        }
        if (request.getPhotoUrl() != null) {
            person.setPhotoUrl(request.getPhotoUrl());
        }
        if (StringUtils.hasText(request.getStatus())) {
            person.setStatus(parsePersonStatus(request.getStatus()));
        }
        if (request.getRemark() != null) {
            person.setRemark(request.getRemark());
        }

        return convertPerson(pickupPersonRepository.save(person));
    }

    public List<PickupPersonResponse> getBabyPickupPersons(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);
        return pickupPersonRepository.findByEnrollmentBabyOrderByCreatedAtDesc(baby).stream()
                .map(this::convertPerson)
                .collect(Collectors.toList());
    }

    public List<PickupPersonResponse> getClassroomPickupPersons(String username, Long classroomId) {
        User operator = getUser(username);
        Classroom classroom = getOwnedClassroom(operator, classroomId);
        return pickupPersonRepository.findByEnrollmentClassroomOrderByCreatedAtDesc(classroom).stream()
                .map(this::convertPerson)
                .collect(Collectors.toList());
    }

    @Transactional
    public PickupDelegationResponse createDelegation(String username, PickupDelegationCreateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getAccessibleEnrollment(operator, request.getEnrollmentId());

        PickupDelegation delegation = new PickupDelegation();
        delegation.setEnrollment(enrollment);
        delegation.setPickupDate(request.getPickupDate());
        delegation.setPickupPersonName(request.getPickupPersonName());
        delegation.setPickupRelationship(request.getPickupRelationship());
        delegation.setPickupPhone(request.getPickupPhone());
        delegation.setReason(request.getReason());
        delegation.setRequestedBy(operator);

        return convertDelegation(pickupDelegationRepository.save(delegation));
    }

    @Transactional
    public PickupDelegationResponse approveDelegation(
            String username,
            Long delegationId,
            PickupDelegationReviewRequest request) {
        User operator = getUser(username);
        PickupDelegation delegation = getOwnedDelegation(operator, delegationId);
        delegation.setStatus(PickupDelegation.DelegationStatus.APPROVED);
        delegation.setPickupCode(generatePickupCode());
        delegation.setReviewedBy(operator);
        delegation.setReviewedAt(LocalDateTime.now());
        delegation.setReviewRemark(request.getReviewRemark());
        return convertDelegation(pickupDelegationRepository.save(delegation));
    }

    @Transactional
    public PickupDelegationResponse rejectDelegation(
            String username,
            Long delegationId,
            PickupDelegationReviewRequest request) {
        User operator = getUser(username);
        PickupDelegation delegation = getOwnedDelegation(operator, delegationId);
        delegation.setStatus(PickupDelegation.DelegationStatus.REJECTED);
        delegation.setReviewedBy(operator);
        delegation.setReviewedAt(LocalDateTime.now());
        delegation.setReviewRemark(request.getReviewRemark());
        return convertDelegation(pickupDelegationRepository.save(delegation));
    }

    public List<PickupDelegationResponse> getBabyDelegations(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);
        return pickupDelegationRepository.findByEnrollmentBabyOrderByPickupDateDescCreatedAtDesc(baby).stream()
                .map(this::convertDelegation)
                .collect(Collectors.toList());
    }

    public List<PickupDelegationResponse> getClassroomDelegations(
            String username,
            Long classroomId,
            LocalDate pickupDate) {
        User operator = getUser(username);
        Classroom classroom = getOwnedClassroom(operator, classroomId);
        LocalDate date = pickupDate != null ? pickupDate : LocalDate.now();
        return pickupDelegationRepository
                .findByEnrollmentClassroomAndPickupDateOrderByCreatedAtDesc(classroom, date)
                .stream()
                .map(this::convertDelegation)
                .collect(Collectors.toList());
    }

    private AuthorizedPickupPerson getAccessiblePickupPerson(User user, Long personId) {
        AuthorizedPickupPerson person = pickupPersonRepository.findById(personId)
                .orElseThrow(() -> new BusinessException("授权接送人不存在"));
        if (!canAccessEnrollment(user, person.getEnrollment())) {
            throw new BusinessException("您无权操作该授权接送人");
        }
        return person;
    }

    private PickupDelegation getOwnedDelegation(User user, Long delegationId) {
        PickupDelegation delegation = pickupDelegationRepository.findById(delegationId)
                .orElseThrow(() -> new BusinessException("委托接送申请不存在"));
        if (!canAccessOrganization(user, delegation.getEnrollment().getOrganization().getId())) {
            throw new BusinessException("您无权审核该委托接送申请");
        }
        return delegation;
    }

    private Enrollment getAccessibleEnrollment(User user, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!canAccessEnrollment(user, enrollment)) {
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
            throw new BusinessException("您无权访问该宝宝接送信息");
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

    private AuthorizedPickupPerson.PickupPersonStatus parsePersonStatus(String status) {
        try {
            return AuthorizedPickupPerson.PickupPersonStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("接送人状态不正确");
        }
    }

    private String generatePickupCode() {
        return String.valueOf(100000 + new Random().nextInt(900000));
    }

    private PickupPersonResponse convertPerson(AuthorizedPickupPerson person) {
        PickupPersonResponse response = new PickupPersonResponse();
        response.setId(person.getId());
        response.setEnrollmentId(person.getEnrollment().getId());
        response.setBabyId(person.getEnrollment().getBaby().getId());
        response.setBabyName(person.getEnrollment().getBaby().getName());
        response.setClassroomId(person.getEnrollment().getClassroom().getId());
        response.setClassroomName(person.getEnrollment().getClassroom().getName());
        response.setOrganizationId(person.getEnrollment().getOrganization().getId());
        response.setOrganizationName(person.getEnrollment().getOrganization().getName());
        response.setName(person.getName());
        response.setRelationship(person.getRelationship());
        response.setPhone(person.getPhone());
        response.setIdentityNo(person.getIdentityNo());
        response.setPhotoUrl(person.getPhotoUrl());
        response.setStatus(person.getStatus());
        response.setStatusDescription(person.getStatus().getDescription());
        response.setRemark(person.getRemark());
        if (person.getCreatedBy() != null) {
            response.setCreatedById(person.getCreatedBy().getId());
            response.setCreatedByName(person.getCreatedBy().getNickname());
        }
        response.setCreatedAt(person.getCreatedAt());
        response.setUpdatedAt(person.getUpdatedAt());
        return response;
    }

    private PickupDelegationResponse convertDelegation(PickupDelegation delegation) {
        PickupDelegationResponse response = new PickupDelegationResponse();
        response.setId(delegation.getId());
        response.setEnrollmentId(delegation.getEnrollment().getId());
        response.setBabyId(delegation.getEnrollment().getBaby().getId());
        response.setBabyName(delegation.getEnrollment().getBaby().getName());
        response.setClassroomId(delegation.getEnrollment().getClassroom().getId());
        response.setClassroomName(delegation.getEnrollment().getClassroom().getName());
        response.setOrganizationId(delegation.getEnrollment().getOrganization().getId());
        response.setOrganizationName(delegation.getEnrollment().getOrganization().getName());
        response.setPickupDate(delegation.getPickupDate());
        response.setPickupPersonName(delegation.getPickupPersonName());
        response.setPickupRelationship(delegation.getPickupRelationship());
        response.setPickupPhone(delegation.getPickupPhone());
        response.setReason(delegation.getReason());
        response.setStatus(delegation.getStatus());
        response.setStatusDescription(delegation.getStatus().getDescription());
        response.setPickupCode(delegation.getPickupCode());
        response.setRequestedById(delegation.getRequestedBy().getId());
        response.setRequestedByName(delegation.getRequestedBy().getNickname());
        if (delegation.getReviewedBy() != null) {
            response.setReviewedById(delegation.getReviewedBy().getId());
            response.setReviewedByName(delegation.getReviewedBy().getNickname());
        }
        response.setReviewedAt(delegation.getReviewedAt());
        response.setReviewRemark(delegation.getReviewRemark());
        response.setCreatedAt(delegation.getCreatedAt());
        response.setUpdatedAt(delegation.getUpdatedAt());
        return response;
    }
}
