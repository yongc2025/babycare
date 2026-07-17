package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.SafetyLedgerHandleRequest;
import com.huigrowth.babycare.dto.SafetyLedgerRequest;
import com.huigrowth.babycare.dto.SafetyLedgerResponse;
import com.huigrowth.babycare.entity.IncidentReport;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.SafetyLedger;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.IncidentReportRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.SafetyLedgerRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SafetyLedgerService {

    private final SafetyLedgerRepository safetyLedgerRepository;
    private final OrganizationRepository organizationRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final UserRepository userRepository;

    @Transactional
    public SafetyLedgerResponse createLedger(String username, SafetyLedgerRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        SafetyLedger ledger = new SafetyLedger();
        ledger.setOrganization(organization);
        ledger.setCreatedBy(operator);
        applyFields(ledger, request, organization);
        return convert(safetyLedgerRepository.save(ledger));
    }

    @Transactional
    public SafetyLedgerResponse updateLedger(String username, Long ledgerId, SafetyLedgerRequest request) {
        User operator = getUser(username);
        SafetyLedger ledger = getOwnedLedger(operator, ledgerId);
        applyFields(ledger, request, ledger.getOrganization());
        return convert(safetyLedgerRepository.save(ledger));
    }

    @Transactional
    public SafetyLedgerResponse markProcessing(
            String username,
            Long ledgerId,
            SafetyLedgerHandleRequest request) {
        User operator = getUser(username);
        SafetyLedger ledger = getOwnedLedger(operator, ledgerId);
        ledger.setStatus(SafetyLedger.LedgerStatus.PROCESSING);
        ledger.setHandledBy(operator);
        if (request != null && request.getHandleRemark() != null) {
            ledger.setHandleRemark(request.getHandleRemark());
        }
        return convert(safetyLedgerRepository.save(ledger));
    }

    @Transactional
    public SafetyLedgerResponse closeLedger(
            String username,
            Long ledgerId,
            SafetyLedgerHandleRequest request) {
        User operator = getUser(username);
        SafetyLedger ledger = getOwnedLedger(operator, ledgerId);
        ledger.setStatus(SafetyLedger.LedgerStatus.CLOSED);
        ledger.setHandledBy(operator);
        ledger.setCompletedAt(LocalDateTime.now());
        if (request != null && request.getHandleRemark() != null) {
            ledger.setHandleRemark(request.getHandleRemark());
        }
        return convert(safetyLedgerRepository.save(ledger));
    }

    public List<SafetyLedgerResponse> getOrganizationLedgers(
            String username,
            Long organizationId,
            LocalDate startDate,
            LocalDate endDate,
            String type,
            String status) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, organizationId);
        if (StringUtils.hasText(type)) {
            return safetyLedgerRepository
                    .findByOrganizationAndLedgerTypeOrderByLedgerDateDescCreatedAtDesc(
                            organization,
                            parseType(type))
                    .stream()
                    .filter(ledger -> matchesStatus(ledger, status))
                    .filter(ledger -> matchesDateRange(ledger, startDate, endDate))
                    .map(this::convert)
                    .collect(Collectors.toList());
        }
        if (StringUtils.hasText(status)) {
            return safetyLedgerRepository
                    .findByOrganizationAndStatusOrderByLedgerDateDescCreatedAtDesc(
                            organization,
                            parseStatus(status))
                    .stream()
                    .filter(ledger -> matchesDateRange(ledger, startDate, endDate))
                    .map(this::convert)
                    .collect(Collectors.toList());
        }
        if (startDate != null || endDate != null) {
            LocalDate start = startDate != null ? startDate : LocalDate.now();
            LocalDate end = endDate != null ? endDate : start;
            if (start.isAfter(end)) {
                throw new BusinessException("开始日期不能晚于结束日期");
            }
            return safetyLedgerRepository
                    .findByOrganizationAndLedgerDateBetweenOrderByLedgerDateDescCreatedAtDesc(
                            organization,
                            start,
                            end)
                    .stream()
                    .map(this::convert)
                    .collect(Collectors.toList());
        }
        return safetyLedgerRepository.findByOrganizationOrderByLedgerDateDescCreatedAtDesc(organization).stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public SafetyLedgerResponse getLedgerDetail(String username, Long ledgerId) {
        User operator = getUser(username);
        return convert(getOwnedLedger(operator, ledgerId));
    }

    private void applyFields(SafetyLedger ledger, SafetyLedgerRequest request, Organization organization) {
        ledger.setLedgerDate(request.getLedgerDate());
        ledger.setLedgerType(parseType(request.getLedgerType()));
        ledger.setTitle(request.getTitle());
        ledger.setContent(request.getContent());
        ledger.setLocation(request.getLocation());
        ledger.setResponsiblePerson(request.getResponsiblePerson());
        ledger.setDueAt(request.getDueAt());
        ledger.setHandleRemark(request.getHandleRemark());
        ledger.setRelatedIncident(resolveIncident(request.getRelatedIncidentId(), organization));
        if (StringUtils.hasText(request.getStatus())) {
            ledger.setStatus(parseStatus(request.getStatus()));
        }
    }

    private IncidentReport resolveIncident(Long incidentId, Organization organization) {
        if (incidentId == null) {
            return null;
        }
        IncidentReport incident = incidentReportRepository.findById(incidentId)
                .orElseThrow(() -> new BusinessException("异常事故记录不存在"));
        if (!incident.getEnrollment().getOrganization().getId().equals(organization.getId())) {
            throw new BusinessException("关联事故不属于该机构");
        }
        return incident;
    }

    private SafetyLedger getOwnedLedger(User operator, Long ledgerId) {
        SafetyLedger ledger = safetyLedgerRepository.findById(ledgerId)
                .orElseThrow(() -> new BusinessException("安全卫生台账不存在"));
        if (!canAccessOrganization(operator, ledger.getOrganization().getId())) {
            throw new BusinessException("您无权操作该安全卫生台账");
        }
        return ledger;
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

    private boolean matchesDateRange(SafetyLedger ledger, LocalDate startDate, LocalDate endDate) {
        if (startDate != null && ledger.getLedgerDate().isBefore(startDate)) {
            return false;
        }
        return endDate == null || !ledger.getLedgerDate().isAfter(endDate);
    }

    private boolean matchesStatus(SafetyLedger ledger, String status) {
        return !StringUtils.hasText(status) || ledger.getStatus() == parseStatus(status);
    }

    private SafetyLedger.LedgerType parseType(String type) {
        try {
            return SafetyLedger.LedgerType.valueOf(type);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("台账类型不正确");
        }
    }

    private SafetyLedger.LedgerStatus parseStatus(String status) {
        try {
            return SafetyLedger.LedgerStatus.valueOf(status);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("台账状态不正确");
        }
    }

    private SafetyLedgerResponse convert(SafetyLedger ledger) {
        SafetyLedgerResponse response = new SafetyLedgerResponse();
        response.setId(ledger.getId());
        response.setOrganizationId(ledger.getOrganization().getId());
        response.setOrganizationName(ledger.getOrganization().getName());
        if (ledger.getRelatedIncident() != null) {
            response.setRelatedIncidentId(ledger.getRelatedIncident().getId());
            response.setRelatedIncidentTitle(ledger.getRelatedIncident().getTitle());
        }
        response.setLedgerDate(ledger.getLedgerDate());
        response.setLedgerType(ledger.getLedgerType());
        response.setLedgerTypeDescription(ledger.getLedgerType().getDescription());
        response.setTitle(ledger.getTitle());
        response.setContent(ledger.getContent());
        response.setLocation(ledger.getLocation());
        response.setResponsiblePerson(ledger.getResponsiblePerson());
        response.setDueAt(ledger.getDueAt());
        response.setCompletedAt(ledger.getCompletedAt());
        response.setStatus(ledger.getStatus());
        response.setStatusDescription(ledger.getStatus().getDescription());
        response.setHandleRemark(ledger.getHandleRemark());
        if (ledger.getCreatedBy() != null) {
            response.setCreatedById(ledger.getCreatedBy().getId());
            response.setCreatedByName(ledger.getCreatedBy().getNickname());
        }
        if (ledger.getHandledBy() != null) {
            response.setHandledById(ledger.getHandledBy().getId());
            response.setHandledByName(ledger.getHandledBy().getNickname());
        }
        response.setCreatedAt(ledger.getCreatedAt());
        response.setUpdatedAt(ledger.getUpdatedAt());
        return response;
    }
}
