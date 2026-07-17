package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.CareRecordCreateRequest;
import com.huigrowth.babycare.dto.CareRecordResponse;
import com.huigrowth.babycare.dto.CareRecordUpdateRequest;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.CareRecord;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.CareRecordRepository;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CareRecordService {

    private final CareRecordRepository careRecordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final BabyRepository babyRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public CareRecordResponse createRecord(String username, CareRecordCreateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, request.getEnrollmentId());

        CareRecord record = new CareRecord();
        record.setEnrollment(enrollment);
        record.setRecordDate(resolveDate(request.getRecordDate()));
        record.setRecordTime(resolveTime(request.getRecordTime()));
        record.setType(parseCareType(request.getType()));
        applyEditableFields(record, request.getValueText(), request.getAmount(), request.getUnit(),
                request.getStartedAt(), request.getEndedAt(), request.getRemark(), request.getSource());
        record.setRecordedBy(operator);

        CareRecord saved = careRecordRepository.save(record);
        log.info("用户 {} 为宝宝 {} 新增照护记录 {}", username, enrollment.getBaby().getName(), record.getType());
        return convert(saved);
    }

    @Transactional
    public CareRecordResponse updateRecord(String username, Long recordId, CareRecordUpdateRequest request) {
        User operator = getUser(username);
        CareRecord record = getOwnedRecord(operator, recordId);

        if (request.getRecordDate() != null) {
            record.setRecordDate(request.getRecordDate());
        }
        if (request.getRecordTime() != null) {
            record.setRecordTime(request.getRecordTime());
        }
        if (StringUtils.hasText(request.getType())) {
            record.setType(parseCareType(request.getType()));
        }
        applyEditableFields(record, request.getValueText(), request.getAmount(), request.getUnit(),
                request.getStartedAt(), request.getEndedAt(), request.getRemark(), request.getSource());
        record.setRecordedBy(operator);

        return convert(careRecordRepository.save(record));
    }

    @Transactional
    public void deleteRecord(String username, Long recordId) {
        User operator = getUser(username);
        CareRecord record = getOwnedRecord(operator, recordId);
        careRecordRepository.delete(record);
        log.info("用户 {} 删除照护记录 {}", username, recordId);
    }

    public List<CareRecordResponse> getClassroomRecords(String username, Long classroomId, LocalDate date) {
        User operator = getUser(username);
        Classroom classroom = getOwnedClassroom(operator, classroomId);
        return careRecordRepository
                .findByEnrollmentClassroomAndRecordDateOrderByRecordTimeDesc(classroom, resolveDate(date))
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public List<CareRecordResponse> getBabyRecords(String username, Long babyId, LocalDate date) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);
        return careRecordRepository
                .findByEnrollmentBabyAndRecordDateOrderByRecordTimeDesc(baby, resolveDate(date))
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public List<CareRecordResponse> getEnrollmentRecords(String username, Long enrollmentId, LocalDate date) {
        User operator = getUser(username);
        Enrollment enrollment = getAccessibleEnrollment(operator, enrollmentId);
        return careRecordRepository
                .findByEnrollmentAndRecordDateOrderByRecordTimeDesc(enrollment, resolveDate(date))
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private CareRecord getOwnedRecord(User user, Long recordId) {
        CareRecord record = careRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException("照护记录不存在"));
        if (!canAccessOrganization(user, record.getEnrollment().getOrganization().getId())) {
            throw new BusinessException("您无权操作该照护记录");
        }
        return record;
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
            throw new BusinessException("您无权访问该宝宝照护记录");
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

    private CareRecord.CareType parseCareType(String type) {
        try {
            return CareRecord.CareType.valueOf(type);
        } catch (IllegalArgumentException error) {
            throw new BusinessException("照护类型不正确");
        }
    }

    private void applyEditableFields(
            CareRecord record,
            String valueText,
            Double amount,
            String unit,
            LocalDateTime startedAt,
            LocalDateTime endedAt,
            String remark,
            String source) {
        record.setValueText(valueText);
        record.setAmount(amount);
        record.setUnit(unit);
        record.setStartedAt(startedAt);
        record.setEndedAt(endedAt);
        record.setRemark(remark);
        if (StringUtils.hasText(source)) {
            record.setSource(source);
        }
    }

    private CareRecordResponse convert(CareRecord record) {
        CareRecordResponse response = new CareRecordResponse();
        response.setId(record.getId());
        response.setEnrollmentId(record.getEnrollment().getId());
        response.setBabyId(record.getEnrollment().getBaby().getId());
        response.setBabyName(record.getEnrollment().getBaby().getName());
        response.setClassroomId(record.getEnrollment().getClassroom().getId());
        response.setClassroomName(record.getEnrollment().getClassroom().getName());
        response.setOrganizationId(record.getEnrollment().getOrganization().getId());
        response.setOrganizationName(record.getEnrollment().getOrganization().getName());
        response.setRecordDate(record.getRecordDate());
        response.setRecordTime(record.getRecordTime());
        response.setType(record.getType());
        response.setTypeDescription(record.getType().getDescription());
        response.setValueText(record.getValueText());
        response.setAmount(record.getAmount());
        response.setUnit(record.getUnit());
        response.setStartedAt(record.getStartedAt());
        response.setEndedAt(record.getEndedAt());
        response.setRemark(record.getRemark());
        response.setSource(record.getSource());
        if (record.getRecordedBy() != null) {
            response.setRecordedById(record.getRecordedBy().getId());
            response.setRecordedByName(record.getRecordedBy().getNickname());
        }
        response.setCreatedAt(record.getCreatedAt());
        response.setUpdatedAt(record.getUpdatedAt());
        return response;
    }
}
