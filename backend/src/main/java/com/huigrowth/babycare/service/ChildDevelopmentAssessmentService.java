package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.ChildDevelopmentAssessmentRequest;
import com.huigrowth.babycare.dto.ChildDevelopmentAssessmentResponse;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.ChildDevelopmentAssessment;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.ChildDevelopmentAssessmentRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChildDevelopmentAssessmentService {

    private final ChildDevelopmentAssessmentRepository assessmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final BabyRepository babyRepository;
    private final OrganizationRepository organizationRepository;
    private final FamilyMemberRepository familyMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChildDevelopmentAssessmentResponse createAssessment(
            String username,
            ChildDevelopmentAssessmentRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getAccessibleEnrollment(operator, request.getEnrollmentId());
        ChildDevelopmentAssessment assessment = new ChildDevelopmentAssessment();
        assessment.setEnrollment(enrollment);
        assessment.setAssessedBy(operator);
        applyFields(assessment, request);
        return convert(assessmentRepository.save(assessment));
    }

    @Transactional
    public ChildDevelopmentAssessmentResponse updateAssessment(
            String username,
            Long assessmentId,
            ChildDevelopmentAssessmentRequest request) {
        User operator = getUser(username);
        ChildDevelopmentAssessment assessment = getOwnedAssessment(operator, assessmentId);
        Enrollment enrollment = getAccessibleEnrollment(operator, request.getEnrollmentId());
        assessment.setEnrollment(enrollment);
        applyFields(assessment, request);
        return convert(assessmentRepository.save(assessment));
    }

    public List<ChildDevelopmentAssessmentResponse> getBabyAssessments(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = babyRepository.findById(babyId)
                .orElseThrow(() -> new BusinessException("宝宝不存在"));
        if (!canAccessBaby(operator, baby) && enrollmentRepository.findByBabyOrderByCreatedAtDesc(baby).stream()
                .noneMatch(enrollment -> canAccessOrganization(operator, enrollment.getOrganization().getId()))) {
            throw new BusinessException("您无权查看该宝宝发展评估");
        }
        return assessmentRepository.findByEnrollmentBabyOrderByAssessmentDateDescCreatedAtDesc(baby).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public List<ChildDevelopmentAssessmentResponse> getEnrollmentAssessments(
            String username,
            Long enrollmentId,
            String assessmentMode) {
        User operator = getUser(username);
        Enrollment enrollment = getAccessibleEnrollment(operator, enrollmentId);
        if (StringUtils.hasText(assessmentMode)) {
            return assessmentRepository
                    .findByEnrollmentAndAssessmentModeOrderByAssessmentDateDescCreatedAtDesc(
                            enrollment,
                            parseMode(assessmentMode))
                    .stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
        }
        return assessmentRepository.findByEnrollmentOrderByAssessmentDateDescCreatedAtDesc(enrollment).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public ChildDevelopmentAssessmentResponse getAssessmentDetail(String username, Long assessmentId) {
        User operator = getUser(username);
        return convert(getOwnedAssessment(operator, assessmentId));
    }

    private void applyFields(
            ChildDevelopmentAssessment assessment,
            ChildDevelopmentAssessmentRequest request) {
        assessment.setAssessmentDate(request.getAssessmentDate());
        assessment.setChildAgeMonths(request.getChildAgeMonths());
        assessment.setAssessmentMode(parseMode(request.getAssessmentMode()));
        assessment.setTitle(request.getTitle());
        assessment.setGrossMotorScore(request.getGrossMotorScore());
        assessment.setFineMotorScore(request.getFineMotorScore());
        assessment.setLanguageScore(request.getLanguageScore());
        assessment.setCognitiveScore(request.getCognitiveScore());
        assessment.setSocialEmotionalScore(request.getSocialEmotionalScore());
        assessment.setHealthScore(request.getHealthScore());
        assessment.setScienceScore(request.getScienceScore());
        assessment.setArtScore(request.getArtScore());
        assessment.setMaxScore(request.getMaxScore() != null ? request.getMaxScore() : 100);
        if (StringUtils.hasText(request.getOverallLevel())) {
            assessment.setOverallLevel(parseLevel(request.getOverallLevel()));
        }
        assessment.setSummary(request.getSummary());
        assessment.setRecommendation(request.getRecommendation());
        assessment.setRadarData(request.getRadarData());
    }

    private ChildDevelopmentAssessment getOwnedAssessment(User operator, Long assessmentId) {
        ChildDevelopmentAssessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new BusinessException("发展评估记录不存在"));
        if (!canAccessEnrollment(operator, assessment.getEnrollment())) {
            throw new BusinessException("您无权操作该发展评估记录");
        }
        return assessment;
    }

    private Enrollment getAccessibleEnrollment(User operator, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!canAccessEnrollment(operator, enrollment)) {
            throw new BusinessException("您无权访问该入托档案");
        }
        return enrollment;
    }

    private boolean canAccessEnrollment(User user, Enrollment enrollment) {
        return canAccessOrganization(user, enrollment.getOrganization().getId())
                || canAccessBaby(user, enrollment.getBaby());
    }

    private boolean canAccessOrganization(User user, Long organizationId) {
        return organizationRepository.existsByIdAndCreatedBy(organizationId, user);
    }

    private boolean canAccessBaby(User user, Baby baby) {
        return familyMemberRepository.existsByUserAndBaby(user, baby.getFamily().getId());
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private ChildDevelopmentAssessment.AssessmentMode parseMode(String mode) {
        try {
            return ChildDevelopmentAssessment.AssessmentMode.valueOf(mode);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("评估模式不正确");
        }
    }

    private ChildDevelopmentAssessment.DevelopmentLevel parseLevel(String level) {
        try {
            return ChildDevelopmentAssessment.DevelopmentLevel.valueOf(level);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("发展水平不正确");
        }
    }

    private ChildDevelopmentAssessmentResponse convert(ChildDevelopmentAssessment assessment) {
        Enrollment enrollment = assessment.getEnrollment();
        ChildDevelopmentAssessmentResponse response = new ChildDevelopmentAssessmentResponse();
        response.setId(assessment.getId());
        response.setEnrollmentId(enrollment.getId());
        response.setBabyId(enrollment.getBaby().getId());
        response.setBabyName(enrollment.getBaby().getName());
        response.setOrganizationId(enrollment.getOrganization().getId());
        response.setOrganizationName(enrollment.getOrganization().getName());
        response.setClassroomId(enrollment.getClassroom().getId());
        response.setClassroomName(enrollment.getClassroom().getName());
        response.setAssessmentDate(assessment.getAssessmentDate());
        response.setChildAgeMonths(assessment.getChildAgeMonths());
        response.setAssessmentMode(assessment.getAssessmentMode());
        response.setAssessmentModeDescription(assessment.getAssessmentMode().getDescription());
        response.setTitle(assessment.getTitle());
        response.setGrossMotorScore(assessment.getGrossMotorScore());
        response.setFineMotorScore(assessment.getFineMotorScore());
        response.setLanguageScore(assessment.getLanguageScore());
        response.setCognitiveScore(assessment.getCognitiveScore());
        response.setSocialEmotionalScore(assessment.getSocialEmotionalScore());
        response.setHealthScore(assessment.getHealthScore());
        response.setScienceScore(assessment.getScienceScore());
        response.setArtScore(assessment.getArtScore());
        response.setMaxScore(assessment.getMaxScore());
        response.setOverallLevel(assessment.getOverallLevel());
        response.setOverallLevelDescription(assessment.getOverallLevel().getDescription());
        response.setSummary(assessment.getSummary());
        response.setRecommendation(assessment.getRecommendation());
        response.setRadarData(assessment.getRadarData());
        if (assessment.getAssessedBy() != null) {
            response.setAssessedById(assessment.getAssessedBy().getId());
            response.setAssessedByName(assessment.getAssessedBy().getNickname());
        }
        response.setCreatedAt(assessment.getCreatedAt());
        response.setUpdatedAt(assessment.getUpdatedAt());
        return response;
    }
}
