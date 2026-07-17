package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.HealthObservationCreateRequest;
import com.huigrowth.babycare.dto.HealthObservationResponse;
import com.huigrowth.babycare.dto.HealthObservationUpdateRequest;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.HealthObservation;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.HealthObservationRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthObservationService {

    private final HealthObservationRepository healthObservationRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public HealthObservationResponse createObservation(String username, HealthObservationCreateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, request.getEnrollmentId());

        HealthObservation observation = new HealthObservation();
        observation.setEnrollment(enrollment);
        observation.setObservationDate(resolveDate(request.getObservationDate()));
        observation.setObservationTime(resolveTime(request.getObservationTime()));
        observation.setType(parseType(request.getType()));
        applyFields(observation, request.getTemperature(), request.getTouchStatus(), request.getLookStatus(),
                request.getAskStatus(), request.getCheckStatus(), request.getSymptoms(), request.getActionTaken(),
                request.getAbnormal(), request.getFollowUpRequired(), request.getSource());
        observation.setRecordedBy(operator);

        HealthObservation saved = healthObservationRepository.save(observation);
        log.info("用户 {} 为宝宝 {} 新增健康观察 {}", username, enrollment.getBaby().getName(), observation.getType());
        return convert(saved);
    }

    @Transactional
    public HealthObservationResponse updateObservation(
            String username,
            Long observationId,
            HealthObservationUpdateRequest request) {
        User operator = getUser(username);
        HealthObservation observation = getOwnedObservation(operator, observationId);

        if (request.getObservationDate() != null) {
            observation.setObservationDate(request.getObservationDate());
        }
        if (request.getObservationTime() != null) {
            observation.setObservationTime(request.getObservationTime());
        }
        if (StringUtils.hasText(request.getType())) {
            observation.setType(parseType(request.getType()));
        }
        applyFields(observation, request.getTemperature(), request.getTouchStatus(), request.getLookStatus(),
                request.getAskStatus(), request.getCheckStatus(), request.getSymptoms(), request.getActionTaken(),
                request.getAbnormal(), request.getFollowUpRequired(), request.getSource());
        observation.setRecordedBy(operator);

        return convert(healthObservationRepository.save(observation));
    }

    @Transactional
    public void deleteObservation(String username, Long observationId) {
        User operator = getUser(username);
        HealthObservation observation = getOwnedObservation(operator, observationId);
        healthObservationRepository.delete(observation);
    }

    public List<HealthObservationResponse> getClassroomObservations(String username, Long classroomId, LocalDate date) {
        User operator = getUser(username);
        Classroom classroom = getOwnedClassroom(operator, classroomId);
        return healthObservationRepository
                .findByEnrollmentClassroomAndObservationDateOrderByObservationTimeDesc(classroom, resolveDate(date))
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public List<HealthObservationResponse> getBabyObservations(String username, Long babyId, LocalDate date) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);
        return healthObservationRepository
                .findByEnrollmentBabyAndObservationDateOrderByObservationTimeDesc(baby, resolveDate(date))
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public List<HealthObservationResponse> getEnrollmentObservations(
            String username,
            Long enrollmentId,
            LocalDate date) {
        User operator = getUser(username);
        Enrollment enrollment = getAccessibleEnrollment(operator, enrollmentId);
        return healthObservationRepository
                .findByEnrollmentAndObservationDateOrderByObservationTimeDesc(enrollment, resolveDate(date))
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private HealthObservation getOwnedObservation(User user, Long observationId) {
        HealthObservation observation = healthObservationRepository.findById(observationId)
                .orElseThrow(() -> new BusinessException("健康观察记录不存在"));
        if (!canAccessOrganization(user, observation.getEnrollment().getOrganization().getId())) {
            throw new BusinessException("您无权操作该健康观察记录");
        }
        return observation;
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

    private Baby getAccessibleBaby(User user, Long babyId) {
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException("宝宝不存在"));
        if (canAccessBaby(user, baby)) {
            return baby;
        }
        boolean hasOwnedEnrollment = enrollmentRepository.findByBabyOrderByCreatedAtDesc(baby).stream()
                .anyMatch(enrollment -> canAccessOrganization(user, enrollment.getOrganization().getId()));
        if (!hasOwnedEnrollment) {
            throw new BusinessException("您无权访问该宝宝健康观察记录");
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

    private LocalDate resolveDate(LocalDate date) {
        return date != null ? date : LocalDate.now();
    }

    private LocalDateTime resolveTime(LocalDateTime time) {
        return time != null ? time : LocalDateTime.now();
    }

    private HealthObservation.ObservationType parseType(String type) {
        try {
            return HealthObservation.ObservationType.valueOf(type);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("健康观察类型不正确");
        }
    }

    private void applyFields(
            HealthObservation observation,
            Double temperature,
            String touchStatus,
            String lookStatus,
            String askStatus,
            String checkStatus,
            String symptoms,
            String actionTaken,
            Boolean abnormal,
            Boolean followUpRequired,
            String source) {
        observation.setTemperature(temperature);
        observation.setTouchStatus(touchStatus);
        observation.setLookStatus(lookStatus);
        observation.setAskStatus(askStatus);
        observation.setCheckStatus(checkStatus);
        observation.setSymptoms(symptoms);
        observation.setActionTaken(actionTaken);
        if (abnormal != null) {
            observation.setAbnormal(abnormal);
        }
        if (followUpRequired != null) {
            observation.setFollowUpRequired(followUpRequired);
        }
        if (StringUtils.hasText(source)) {
            observation.setSource(source);
        }
    }

    private HealthObservationResponse convert(HealthObservation observation) {
        HealthObservationResponse response = new HealthObservationResponse();
        response.setId(observation.getId());
        response.setEnrollmentId(observation.getEnrollment().getId());
        response.setBabyId(observation.getEnrollment().getBaby().getId());
        response.setBabyName(observation.getEnrollment().getBaby().getName());
        response.setClassroomId(observation.getEnrollment().getClassroom().getId());
        response.setClassroomName(observation.getEnrollment().getClassroom().getName());
        response.setOrganizationId(observation.getEnrollment().getOrganization().getId());
        response.setOrganizationName(observation.getEnrollment().getOrganization().getName());
        response.setObservationDate(observation.getObservationDate());
        response.setObservationTime(observation.getObservationTime());
        response.setType(observation.getType());
        response.setTypeDescription(observation.getType().getDescription());
        response.setTemperature(observation.getTemperature());
        response.setTouchStatus(observation.getTouchStatus());
        response.setLookStatus(observation.getLookStatus());
        response.setAskStatus(observation.getAskStatus());
        response.setCheckStatus(observation.getCheckStatus());
        response.setSymptoms(observation.getSymptoms());
        response.setActionTaken(observation.getActionTaken());
        response.setAbnormal(observation.getAbnormal());
        response.setFollowUpRequired(observation.getFollowUpRequired());
        response.setSource(observation.getSource());
        if (observation.getRecordedBy() != null) {
            response.setRecordedById(observation.getRecordedBy().getId());
            response.setRecordedByName(observation.getRecordedBy().getNickname());
        }
        response.setCreatedAt(observation.getCreatedAt());
        response.setUpdatedAt(observation.getUpdatedAt());
        return response;
    }
}
