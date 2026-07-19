package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.SafetyLedgerHandleRequest;
import com.huigrowth.babycare.dto.SafetyLedgerOverdueResponse;
import com.huigrowth.babycare.dto.SafetyLedgerRequest;
import com.huigrowth.babycare.dto.SafetyLedgerResponse;
import com.huigrowth.babycare.dto.SafetyLedgerTemplateRequest;
import com.huigrowth.babycare.dto.SafetyLedgerTemplateResponse;
import com.huigrowth.babycare.entity.IncidentReport;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.SafetyLedger;
import com.huigrowth.babycare.entity.SafetyLedgerTemplate;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.IncidentReportRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.SafetyLedgerRepository;
import com.huigrowth.babycare.repository.SafetyLedgerTemplateRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SafetyLedgerService {

    private final SafetyLedgerRepository safetyLedgerRepository;
    private final SafetyLedgerTemplateRepository safetyLedgerTemplateRepository;
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

    // ========== 台账模板 CRUD ==========

    @Transactional
    public SafetyLedgerTemplateResponse createTemplate(String username, SafetyLedgerTemplateRequest request) {
        User operator = getUser(username);
        Organization organization = getOwnedOrganization(operator, request.getOrganizationId());
        SafetyLedgerTemplate template = new SafetyLedgerTemplate();
        applyTemplateFields(template, request, organization);
        return convertTemplate(safetyLedgerTemplateRepository.save(template));
    }

    @Transactional
    public SafetyLedgerTemplateResponse updateTemplate(String username, Long templateId, SafetyLedgerTemplateRequest request) {
        User operator = getUser(username);
        SafetyLedgerTemplate template = getOwnedTemplate(operator, templateId);
        if (request.getOrganizationId() != null && !request.getOrganizationId().equals(template.getOrganizationId())) {
            throw new BusinessException("不允许变更机构");
        }
        applyTemplateFields(template, request,
                organizationRepository.findById(template.getOrganizationId()).orElse(null));
        return convertTemplate(safetyLedgerTemplateRepository.save(template));
    }

    @Transactional
    public void deleteTemplate(String username, Long templateId) {
        User operator = getUser(username);
        SafetyLedgerTemplate template = getOwnedTemplate(operator, templateId);
        safetyLedgerTemplateRepository.delete(template);
    }

    public List<SafetyLedgerTemplateResponse> getOrganizationTemplates(String username, Long organizationId) {
        User operator = getUser(username);
        getOwnedOrganization(operator, organizationId);
        return safetyLedgerTemplateRepository.findByOrganizationId(organizationId).stream()
                .map(this::convertTemplate)
                .collect(Collectors.toList());
    }

    // ========== 周期任务生成 ==========

    /**
     * 根据模板自动生成到期台账。检查所有 active 模板的 nextGenerateDate，
     * 如果到期则生成对应台账，并更新模板的 lastGeneratedAt 和 nextGenerateDate。
     */
    @Transactional
    public int generateTasks(String username, Long organizationId) {
        User operator = getUser(username);
        getOwnedOrganization(operator, organizationId);
        List<SafetyLedgerTemplate> templates = safetyLedgerTemplateRepository
                .findByOrganizationIdAndIsActiveTrue(organizationId);
        int count = 0;
        for (SafetyLedgerTemplate template : templates) {
            if (template.getNextGenerateDate() != null
                    && !template.getNextGenerateDate().isAfter(LocalDate.now())) {
                // 生成台账
                SafetyLedger ledger = new SafetyLedger();
                ledger.setOrganization(organizationRepository.findById(organizationId).orElse(null));
                ledger.setCreatedBy(operator);
                ledger.setLedgerDate(LocalDate.now());
                ledger.setLedgerType(template.getLedgerType());
                ledger.setTitle(template.getTitle());
                ledger.setContent(template.getContent());
                ledger.setLocation(template.getLocation());
                ledger.setResponsiblePerson(template.getResponsiblePerson());
                ledger.setStatus(SafetyLedger.LedgerStatus.OPEN);
                safetyLedgerRepository.save(ledger);
                // 更新模板
                template.setLastGeneratedAt(LocalDate.now());
                template.setNextGenerateDate(calculateNextDate(template));
                safetyLedgerTemplateRepository.save(template);
                count++;
            }
        }
        return count;
    }

    // ========== 逾期检测 ==========

    /**
     * 检查并标记逾期台账（超过 dueAt 且状态为 OPEN 或 PROCESSING 的标记为 OVERDUE）。
     */
    @Transactional
    public int checkOverdue(String username, Long organizationId) {
        User operator = getUser(username);
        getOwnedOrganization(operator, organizationId);
        List<SafetyLedger> ledgers = safetyLedgerRepository
                .findByOrganizationIdAndStatusIn(organizationId,
                        List.of(SafetyLedger.LedgerStatus.OPEN, SafetyLedger.LedgerStatus.PROCESSING));
        int count = 0;
        LocalDateTime now = LocalDateTime.now();
        for (SafetyLedger ledger : ledgers) {
            if (ledger.getDueAt() != null && ledger.getDueAt().isBefore(now)) {
                ledger.setStatus(SafetyLedger.LedgerStatus.OVERDUE);
                safetyLedgerRepository.save(ledger);
                count++;
            }
        }
        return count;
    }

    /**
     * 获取机构台账统计（逾期、待处理、处理中数量）。
     */
    public SafetyLedgerOverdueResponse getOverdueCount(String username, Long organizationId) {
        User operator = getUser(username);
        getOwnedOrganization(operator, organizationId);
        SafetyLedgerOverdueResponse response = new SafetyLedgerOverdueResponse();
        response.setOverdueCount(
                (int) safetyLedgerRepository.countByOrganizationIdAndStatus(organizationId,
                        SafetyLedger.LedgerStatus.OVERDUE));
        response.setOpenCount(
                (int) safetyLedgerRepository.countByOrganizationIdAndStatus(organizationId,
                        SafetyLedger.LedgerStatus.OPEN));
        response.setProcessingCount(
                (int) safetyLedgerRepository.countByOrganizationIdAndStatus(organizationId,
                        SafetyLedger.LedgerStatus.PROCESSING));
        return response;
    }

    // ========== 模板私有方法 ==========

    private SafetyLedgerTemplate getOwnedTemplate(User operator, Long templateId) {
        SafetyLedgerTemplate template = safetyLedgerTemplateRepository.findById(templateId)
                .orElseThrow(() -> new BusinessException("安全台账模板不存在"));
        if (!canAccessOrganization(operator, template.getOrganizationId())) {
            throw new BusinessException("您无权操作该模板");
        }
        return template;
    }

    private void applyTemplateFields(SafetyLedgerTemplate template, SafetyLedgerTemplateRequest request,
            Organization organization) {
        template.setOrganizationId(request.getOrganizationId());
        template.setLedgerType(parseType(request.getLedgerType()));
        template.setFrequency(SafetyLedgerTemplate.Frequency.valueOf(request.getFrequency()));
        template.setDayOfWeek(request.getDayOfWeek());
        template.setDayOfMonth(request.getDayOfMonth());
        template.setTitle(request.getTitle());
        template.setLocation(request.getLocation());
        template.setResponsiblePerson(request.getResponsiblePerson());
        template.setContent(request.getContent());
        template.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        if (request.getNextGenerateDate() != null) {
            template.setNextGenerateDate(request.getNextGenerateDate());
        } else if (template.getNextGenerateDate() == null) {
            template.setNextGenerateDate(calculateNextDate(template));
        }
    }

    /**
     * 根据频率计算下一次生成日期。
     */
    private LocalDate calculateNextDate(SafetyLedgerTemplate template) {
        LocalDate today = LocalDate.now();
        switch (template.getFrequency()) {
            case DAILY:
                return today.plusDays(1);
            case WEEKLY: {
                int dow = template.getDayOfWeek() != null ? template.getDayOfWeek() : 1; // 默认周一
                return today.with(TemporalAdjusters.next(DayOfWeek.of(dow)));
            }
            case BIWEEKLY: {
                int dow = template.getDayOfWeek() != null ? template.getDayOfWeek() : 1;
                return today.with(TemporalAdjusters.next(DayOfWeek.of(dow))).plusWeeks(1);
            }
            case MONTHLY: {
                int dom = template.getDayOfMonth() != null ? template.getDayOfMonth() : 1;
                LocalDate next = today.withDayOfMonth(Math.min(dom, today.lengthOfMonth()));
                if (!next.isAfter(today)) {
                    next = today.plusMonths(1).withDayOfMonth(Math.min(dom, today.plusMonths(1).lengthOfMonth()));
                }
                return next;
            }
            default:
                return today.plusDays(1);
        }
    }

    private SafetyLedgerTemplateResponse convertTemplate(SafetyLedgerTemplate template) {
        SafetyLedgerTemplateResponse response = new SafetyLedgerTemplateResponse();
        response.setId(template.getId());
        response.setOrganizationId(template.getOrganizationId());
        response.setLedgerType(template.getLedgerType().name());
        response.setLedgerTypeDescription(template.getLedgerType().getDescription());
        response.setFrequency(template.getFrequency().name());
        response.setFrequencyDescription(template.getFrequency().getDescription());
        response.setDayOfWeek(template.getDayOfWeek());
        response.setDayOfMonth(template.getDayOfMonth());
        response.setTitle(template.getTitle());
        response.setLocation(template.getLocation());
        response.setResponsiblePerson(template.getResponsiblePerson());
        response.setContent(template.getContent());
        response.setIsActive(template.getIsActive());
        response.setLastGeneratedAt(template.getLastGeneratedAt());
        response.setNextGenerateDate(template.getNextGenerateDate());
        response.setCreatedAt(template.getCreatedAt());
        response.setUpdatedAt(template.getUpdatedAt());
        return response;
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
