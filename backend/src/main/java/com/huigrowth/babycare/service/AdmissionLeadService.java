package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.AdmissionLeadRequest;
import com.huigrowth.babycare.dto.AdmissionLeadResponse;
import com.huigrowth.babycare.dto.AdmissionReviewRequest;
import com.huigrowth.babycare.dto.AdmissionTrialRequest;
import com.huigrowth.babycare.entity.AdmissionLead;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.AdmissionLeadRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
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
public class AdmissionLeadService {

    private final AdmissionLeadRepository admissionLeadRepository;
    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    @Transactional
    public AdmissionLeadResponse createLead(String username, AdmissionLeadRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        AdmissionLead lead = new AdmissionLead();
        lead.setOrganization(organization);
        applyLeadFields(lead, request, organization);
        return convert(admissionLeadRepository.save(lead));
    }

    @Transactional
    public AdmissionLeadResponse updateLead(String username, Long leadId, AdmissionLeadRequest request) {
        User operator = getUser(username);
        AdmissionLead lead = getOwnedLead(operator, leadId);
        applyLeadFields(lead, request, lead.getOrganization());
        return convert(admissionLeadRepository.save(lead));
    }

    @Transactional
    public AdmissionLeadResponse reviewApplication(String username, Long leadId, AdmissionReviewRequest request) {
        User operator = getUser(username);
        AdmissionLead lead = getOwnedLead(operator, leadId);
        AdmissionLead.LeadStatus result = parseReviewResult(request.getResult());
        lead.setStatus(result);
        lead.setReviewedBy(operator);
        lead.setReviewedAt(LocalDateTime.now());
        lead.setReviewRemark(request.getReviewRemark());
        return convert(admissionLeadRepository.save(lead));
    }

    @Transactional
    public AdmissionLeadResponse startTrial(String username, Long leadId, AdmissionTrialRequest request) {
        User operator = getUser(username);
        AdmissionLead lead = getOwnedLead(operator, leadId);
        if (lead.getStatus() != AdmissionLead.LeadStatus.APPROVED
                && lead.getStatus() != AdmissionLead.LeadStatus.TRIAL_COMPLETED) {
            throw new BusinessException("只有审核通过的报名可以开始试托");
        }
        validateTrialDates(request);
        lead.setStatus(AdmissionLead.LeadStatus.TRIALING);
        lead.setTrialStartDate(request.getTrialStartDate());
        lead.setTrialEndDate(request.getTrialEndDate());
        lead.setTrialFeedback(request.getTrialFeedback());
        return convert(admissionLeadRepository.save(lead));
    }

    @Transactional
    public AdmissionLeadResponse finishTrial(String username, Long leadId, AdmissionTrialRequest request) {
        User operator = getUser(username);
        AdmissionLead lead = getOwnedLead(operator, leadId);
        if (lead.getStatus() != AdmissionLead.LeadStatus.TRIALING) {
            throw new BusinessException("只有试托中的线索可以结束试托");
        }
        validateTrialDates(request);
        lead.setStatus(AdmissionLead.LeadStatus.TRIAL_COMPLETED);
        lead.setTrialStartDate(request.getTrialStartDate());
        lead.setTrialEndDate(request.getTrialEndDate());
        lead.setTrialFeedback(request.getTrialFeedback());
        return convert(admissionLeadRepository.save(lead));
    }

    public List<AdmissionLeadResponse> getOrganizationLeads(
            String username,
            Long organizationId,
            String status) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, organizationId);
        if (StringUtils.hasText(status)) {
            return admissionLeadRepository
                    .findByOrganizationAndStatusOrderByCreatedAtDesc(organization, parseStatus(status))
                    .stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
        }
        return admissionLeadRepository.findByOrganizationOrderByCreatedAtDesc(organization).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public AdmissionLeadResponse getLeadDetail(String username, Long leadId) {
        User operator = getUser(username);
        return convert(getOwnedLead(operator, leadId));
    }

    private void applyLeadFields(AdmissionLead lead, AdmissionLeadRequest request, Organization organization) {
        lead.setIntendedClassroom(resolveClassroom(request.getIntendedClassroomId(), organization));
        lead.setChildName(request.getChildName());
        lead.setChildGender(request.getChildGender());
        lead.setChildBirthday(request.getChildBirthday());
        lead.setGuardianName(request.getGuardianName());
        lead.setGuardianPhone(request.getGuardianPhone());
        lead.setPreferredStartDate(request.getPreferredStartDate());
        lead.setRemark(request.getRemark());
        if (StringUtils.hasText(request.getSource())) {
            lead.setSource(parseSource(request.getSource()));
        }
        if (StringUtils.hasText(request.getIntentionLevel())) {
            lead.setIntentionLevel(parseIntentionLevel(request.getIntentionLevel()));
        }
        if (StringUtils.hasText(request.getStatus())) {
            lead.setStatus(parseStatus(request.getStatus()));
        }
    }

    private Classroom resolveClassroom(Long classroomId, Organization organization) {
        if (classroomId == null) {
            return null;
        }
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));
        if (!classroom.getOrganization().getId().equals(organization.getId())) {
            throw new BusinessException("意向班级不属于该机构");
        }
        return classroom;
    }

    private void validateTrialDates(AdmissionTrialRequest request) {
        if (request.getTrialStartDate().isAfter(request.getTrialEndDate())) {
            throw new BusinessException("试托开始日期不能晚于结束日期");
        }
    }

    private AdmissionLead getOwnedLead(User operator, Long leadId) {
        AdmissionLead lead = admissionLeadRepository.findById(leadId)
                .orElseThrow(() -> new BusinessException("招生线索不存在"));
        if (!canAccessOrganization(operator, lead.getOrganization().getId())) {
            throw new BusinessException("您无权操作该招生线索");
        }
        return lead;
    }

    private Organization getOwnedOrganization(User operator, Long organizationId) {
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        if (!canAccessOrganization(operator, organizationId)) {
            throw new BusinessException("您无权访问该机构");
        }
        return organization;
    }

    private boolean canAccessOrganization(User operator, Long organizationId) {
        return organizationRepository.existsByIdAndCreatedBy(organizationId, operator);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private AdmissionLead.LeadSource parseSource(String source) {
        try {
            return AdmissionLead.LeadSource.valueOf(source);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("招生来源不正确");
        }
    }

    private AdmissionLead.IntentionLevel parseIntentionLevel(String level) {
        try {
            return AdmissionLead.IntentionLevel.valueOf(level);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("意向等级不正确");
        }
    }

    private AdmissionLead.LeadStatus parseStatus(String status) {
        try {
            return AdmissionLead.LeadStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("招生线索状态不正确");
        }
    }

    private AdmissionLead.LeadStatus parseReviewResult(String result) {
        AdmissionLead.LeadStatus status = parseStatus(result);
        if (status != AdmissionLead.LeadStatus.APPROVED && status != AdmissionLead.LeadStatus.REJECTED) {
            throw new BusinessException("报名审核结果只能为通过或拒绝");
        }
        return status;
    }

    private AdmissionLeadResponse convert(AdmissionLead lead) {
        AdmissionLeadResponse response = new AdmissionLeadResponse();
        response.setId(lead.getId());
        response.setOrganizationId(lead.getOrganization().getId());
        response.setOrganizationName(lead.getOrganization().getName());
        if (lead.getIntendedClassroom() != null) {
            response.setIntendedClassroomId(lead.getIntendedClassroom().getId());
            response.setIntendedClassroomName(lead.getIntendedClassroom().getName());
        }
        response.setChildName(lead.getChildName());
        response.setChildGender(lead.getChildGender());
        response.setChildBirthday(lead.getChildBirthday());
        response.setGuardianName(lead.getGuardianName());
        response.setGuardianPhone(lead.getGuardianPhone());
        response.setSource(lead.getSource());
        response.setSourceDescription(lead.getSource().getDescription());
        response.setIntentionLevel(lead.getIntentionLevel());
        response.setIntentionLevelDescription(lead.getIntentionLevel().getDescription());
        response.setStatus(lead.getStatus());
        response.setStatusDescription(lead.getStatus().getDescription());
        response.setPreferredStartDate(lead.getPreferredStartDate());
        response.setRemark(lead.getRemark());
        if (lead.getReviewedBy() != null) {
            response.setReviewedById(lead.getReviewedBy().getId());
            response.setReviewedByName(lead.getReviewedBy().getNickname());
        }
        response.setReviewedAt(lead.getReviewedAt());
        response.setReviewRemark(lead.getReviewRemark());
        response.setTrialStartDate(lead.getTrialStartDate());
        response.setTrialEndDate(lead.getTrialEndDate());
        response.setTrialFeedback(lead.getTrialFeedback());
        response.setCreatedAt(lead.getCreatedAt());
        response.setUpdatedAt(lead.getUpdatedAt());
        return response;
    }
}
