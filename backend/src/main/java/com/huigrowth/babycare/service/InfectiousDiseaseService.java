package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.InfectiousDiseaseCreateRequest;
import com.huigrowth.babycare.dto.InfectiousDiseaseResponse;
import com.huigrowth.babycare.dto.InfectiousDiseaseUpdateRequest;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.InfectiousDisease;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.InfectiousDiseaseRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InfectiousDiseaseService {

    private final InfectiousDiseaseRepository infectiousDiseaseRepository;
    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public InfectiousDiseaseResponse createRecord(String username, InfectiousDiseaseCreateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        Organization organization = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new BusinessException("机构不存在"));
        Classroom classroom = classroomRepository.findById(request.getClassroomId())
                .orElseThrow(() -> new BusinessException("班级不存在"));

        InfectiousDisease disease = new InfectiousDisease();
        disease.setEnrollment(enrollment);
        disease.setOrganizationId(request.getOrganizationId());
        disease.setClassroomId(request.getClassroomId());
        disease.setDiseaseName(request.getDiseaseName());
        disease.setSymptoms(request.getSymptoms());
        disease.setOnsetDate(request.getOnsetDate());
        disease.setStatus(parseStatus(request.getStatus()));
        disease.setSeverity(parseSeverity(request.getSeverity()));
        disease.setReportedAt(LocalDateTime.now());
        disease.setReportedBy(operator);
        disease.setTreatmentNotes(request.getTreatmentNotes());
        disease.setParentNotified(request.getParentNotified() != null ? request.getParentNotified() : false);
        disease.setCloseContacts(request.getCloseContacts());
        disease.setClassroomAlertSent(request.getClassroomAlertSent() != null ? request.getClassroomAlertSent() : false);
        disease.setRemark(request.getRemark());

        InfectiousDisease saved = infectiousDiseaseRepository.save(disease);
        log.info("用户 {} 新增传染病记录: {} ({})", username, saved.getDiseaseName(), saved.getStatus().getDescription());
        return convertToResponse(saved);
    }

    @Transactional
    public InfectiousDiseaseResponse updateRecord(String username, Long recordId, InfectiousDiseaseUpdateRequest request) {
        User operator = getUser(username);
        InfectiousDisease disease = infectiousDiseaseRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("传染病记录不存在"));

        if (request.getDiseaseName() != null) disease.setDiseaseName(request.getDiseaseName());
        if (request.getSymptoms() != null) disease.setSymptoms(request.getSymptoms());
        if (request.getOnsetDate() != null) disease.setOnsetDate(request.getOnsetDate());
        if (request.getStatus() != null) disease.setStatus(parseStatus(request.getStatus()));
        if (request.getSeverity() != null) disease.setSeverity(parseSeverity(request.getSeverity()));
        if (request.getIsolationStart() != null) disease.setIsolationStart(request.getIsolationStart());
        if (request.getIsolationEnd() != null) disease.setIsolationEnd(request.getIsolationEnd());
        if (request.getReturnDate() != null) disease.setReturnDate(request.getReturnDate());
        if (request.getTreatmentNotes() != null) disease.setTreatmentNotes(request.getTreatmentNotes());
        if (request.getParentNotified() != null) disease.setParentNotified(request.getParentNotified());
        if (request.getCloseContacts() != null) disease.setCloseContacts(request.getCloseContacts());
        if (request.getClassroomAlertSent() != null) disease.setClassroomAlertSent(request.getClassroomAlertSent());
        if (request.getRemark() != null) disease.setRemark(request.getRemark());

        InfectiousDisease saved = infectiousDiseaseRepository.save(disease);
        log.info("用户 {} 更新传染病记录: {} (ID={})", username, saved.getDiseaseName(), saved.getId());
        return convertToResponse(saved);
    }

    public List<InfectiousDiseaseResponse> getClassroomRecords(String username, Long classroomId) {
        Classroom classroom = classroomRepository.findById(classroomId)
                .orElseThrow(() -> new BusinessException("班级不存在"));
        return infectiousDiseaseRepository.findByClassroomIdOrderByCreatedAtDesc(classroomId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<InfectiousDiseaseResponse> getOrganizationRecords(String username, Long organizationId) {
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        return infectiousDiseaseRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public InfectiousDiseaseResponse getRecordDetail(String username, Long recordId) {
        InfectiousDisease disease = infectiousDiseaseRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("传染病记录不存在"));
        return convertToResponse(disease);
    }

    public long countActiveByOrganization(String username, Long organizationId) {
        organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        return infectiousDiseaseRepository.countByOrganizationIdAndStatusIn(organizationId,
                List.of(InfectiousDisease.DiseaseStatus.SUSPECTED,
                        InfectiousDisease.DiseaseStatus.CONFIRMED,
                        InfectiousDisease.DiseaseStatus.ISOLATED));
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
    }

    private InfectiousDisease.DiseaseStatus parseStatus(String status) {
        try {
            return InfectiousDisease.DiseaseStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("疾病状态不正确");
        }
    }

    private InfectiousDisease.DiseaseSeverity parseSeverity(String severity) {
        try {
            return InfectiousDisease.DiseaseSeverity.valueOf(severity);
        } catch (IllegalArgumentException e) {
            throw new BusinessException("严重程度不正确");
        }
    }

    private InfectiousDiseaseResponse convertToResponse(InfectiousDisease disease) {
        InfectiousDiseaseResponse resp = new InfectiousDiseaseResponse();
        resp.setId(disease.getId());
        resp.setEnrollmentId(disease.getEnrollment().getId());
        resp.setBabyId(disease.getEnrollment().getBaby().getId());
        resp.setBabyName(disease.getEnrollment().getBaby().getName());
        resp.setClassroomId(disease.getClassroomId());
        if (disease.getEnrollment().getClassroom() != null) {
            resp.setClassroomName(disease.getEnrollment().getClassroom().getName());
        }
        resp.setOrganizationId(disease.getOrganizationId());
        resp.setDiseaseName(disease.getDiseaseName());
        resp.setSymptoms(disease.getSymptoms());
        resp.setOnsetDate(disease.getOnsetDate());
        resp.setStatus(disease.getStatus());
        resp.setStatusDescription(disease.getStatus().getDescription());
        resp.setSeverity(disease.getSeverity());
        resp.setSeverityDescription(disease.getSeverity().getDescription());
        resp.setReportedAt(disease.getReportedAt());
        if (disease.getReportedBy() != null) {
            resp.setReportedById(disease.getReportedBy().getId());
            resp.setReportedByName(disease.getReportedBy().getNickname());
        }
        resp.setIsolationStart(disease.getIsolationStart());
        resp.setIsolationEnd(disease.getIsolationEnd());
        resp.setReturnDate(disease.getReturnDate());
        resp.setTreatmentNotes(disease.getTreatmentNotes());
        resp.setParentNotified(disease.getParentNotified());
        resp.setCloseContacts(disease.getCloseContacts());
        resp.setClassroomAlertSent(disease.getClassroomAlertSent());
        resp.setRemark(disease.getRemark());
        resp.setCreatedAt(disease.getCreatedAt());
        resp.setUpdatedAt(disease.getUpdatedAt());
        return resp;
    }
}
