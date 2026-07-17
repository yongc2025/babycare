package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.DailyReportGenerateRequest;
import com.huigrowth.babycare.dto.DailyReportResponse;
import com.huigrowth.babycare.dto.DailyReportUpdateRequest;
import com.huigrowth.babycare.entity.AttendanceRecord;
import com.huigrowth.babycare.entity.Baby;
import com.huigrowth.babycare.entity.CareRecord;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.DailyReport;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.HealthObservation;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.AttendanceRecordRepository;
import com.huigrowth.babycare.repository.BabyRepository;
import com.huigrowth.babycare.repository.CareRecordRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.DailyReportRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.FamilyMemberRepository;
import com.huigrowth.babycare.repository.HealthObservationRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyReportService {

    private final DailyReportRepository dailyReportRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final CareRecordRepository careRecordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ClassroomRepository classroomRepository;
    private final BabyRepository babyRepository;
    private final HealthObservationRepository healthObservationRepository;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final FamilyMemberRepository familyMemberRepository;

    @Transactional
    public DailyReportResponse generateReport(String username, DailyReportGenerateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, request.getEnrollmentId());
        LocalDate reportDate = resolveDate(request.getReportDate());

        DailyReport report = dailyReportRepository.findByEnrollmentAndReportDate(enrollment, reportDate)
                .orElseGet(() -> {
                    DailyReport dailyReport = new DailyReport();
                    dailyReport.setEnrollment(enrollment);
                    dailyReport.setReportDate(reportDate);
                    return dailyReport;
                });

        List<CareRecord> careRecords = careRecordRepository
                .findByEnrollmentAndRecordDateOrderByRecordTimeDesc(enrollment, reportDate);
        AttendanceRecord attendance = attendanceRecordRepository
                .findByEnrollmentAndAttendanceDate(enrollment, reportDate)
                .orElse(null);
        List<HealthObservation> healthObservations = healthObservationRepository
                .findByEnrollmentAndObservationDateOrderByObservationTimeDesc(enrollment, reportDate);

        report.setStatus(DailyReport.ReportStatus.DRAFT);
        report.setAttendanceSummary(buildAttendanceSummary(attendance));
        report.setCareSummary(buildCareSummary(careRecords));
        report.setHealthSummary(buildHealthSummary(careRecords, healthObservations));
        report.setActivitySummary(buildActivitySummary(careRecords));
        report.setSummary(buildSummary(enrollment, reportDate, attendance, careRecords));
        report.setTeacherComment(request.getTeacherComment());

        return convert(dailyReportRepository.save(report));
    }

    @Transactional
    public DailyReportResponse generateAiDraft(String username, DailyReportGenerateRequest request) {
        User operator = getUser(username);
        Enrollment enrollment = getOwnedEnrollment(operator, request.getEnrollmentId());
        LocalDate reportDate = resolveDate(request.getReportDate());

        DailyReport report = dailyReportRepository.findByEnrollmentAndReportDate(enrollment, reportDate)
                .orElseGet(() -> {
                    DailyReport dailyReport = new DailyReport();
                    dailyReport.setEnrollment(enrollment);
                    dailyReport.setReportDate(reportDate);
                    return dailyReport;
                });

        List<CareRecord> careRecords = careRecordRepository
                .findByEnrollmentAndRecordDateOrderByRecordTimeDesc(enrollment, reportDate);
        AttendanceRecord attendance = attendanceRecordRepository
                .findByEnrollmentAndAttendanceDate(enrollment, reportDate)
                .orElse(null);
        List<HealthObservation> healthObservations = healthObservationRepository
                .findByEnrollmentAndObservationDateOrderByObservationTimeDesc(enrollment, reportDate);

        report.setStatus(DailyReport.ReportStatus.DRAFT);
        report.setAttendanceSummary(buildAttendanceSummary(attendance));
        report.setCareSummary(buildCareSummary(careRecords));
        report.setHealthSummary(buildHealthSummary(careRecords, healthObservations));
        report.setActivitySummary(buildActivitySummary(careRecords));
        report.setSummary(buildSummary(enrollment, reportDate, attendance, careRecords));
        if (StringUtils.hasText(request.getTeacherComment())) {
            report.setTeacherComment(request.getTeacherComment());
        }
        report.setAiDraftContent(buildAiDraftContent(enrollment, reportDate, attendance, careRecords, healthObservations));
        return convert(dailyReportRepository.save(report));
    }

    @Transactional
    public DailyReportResponse updateReport(String username, Long reportId, DailyReportUpdateRequest request) {
        User operator = getUser(username);
        DailyReport report = getOwnedReport(operator, reportId);
        applyUpdates(report, request);
        return convert(dailyReportRepository.save(report));
    }

    @Transactional
    public DailyReportResponse publishReport(String username, Long reportId) {
        User operator = getUser(username);
        DailyReport report = getOwnedReport(operator, reportId);
        report.setStatus(DailyReport.ReportStatus.PUBLISHED);
        report.setPublishedAt(LocalDateTime.now());
        report.setPublishedBy(operator);
        return convert(dailyReportRepository.save(report));
    }

    public DailyReportResponse getBabyReport(String username, Long babyId, LocalDate date) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);
        DailyReport report = dailyReportRepository.findByEnrollmentBabyAndReportDate(baby, resolveDate(date))
                .orElseThrow(() -> new BusinessException("日报不存在"));
        assertReportVisible(operator, report);
        return convert(report);
    }

    public List<DailyReportResponse> getBabyReports(String username, Long babyId) {
        User operator = getUser(username);
        Baby baby = getAccessibleBaby(operator, babyId);
        return dailyReportRepository.findByEnrollmentBabyOrderByReportDateDesc(baby).stream()
                .filter(report -> canAccessOrganization(operator, report.getEnrollment().getOrganization().getId())
                        || report.getStatus() == DailyReport.ReportStatus.PUBLISHED)
                .map(this::convert)
                .collect(Collectors.toList());
    }

    public List<DailyReportResponse> getClassroomReports(String username, Long classroomId, LocalDate date) {
        User operator = getUser(username);
        Classroom classroom = getOwnedClassroom(operator, classroomId);
        return dailyReportRepository
                .findByEnrollmentClassroomAndReportDateOrderByCreatedAtDesc(classroom, resolveDate(date))
                .stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }

    private void applyUpdates(DailyReport report, DailyReportUpdateRequest request) {
        if (request.getSummary() != null) {
            report.setSummary(request.getSummary());
        }
        if (request.getAttendanceSummary() != null) {
            report.setAttendanceSummary(request.getAttendanceSummary());
        }
        if (request.getCareSummary() != null) {
            report.setCareSummary(request.getCareSummary());
        }
        if (request.getHealthSummary() != null) {
            report.setHealthSummary(request.getHealthSummary());
        }
        if (request.getActivitySummary() != null) {
            report.setActivitySummary(request.getActivitySummary());
        }
        if (request.getTeacherComment() != null) {
            report.setTeacherComment(request.getTeacherComment());
        }
        if (request.getAiDraftContent() != null) {
            report.setAiDraftContent(request.getAiDraftContent());
        }
    }

    private String buildSummary(
            Enrollment enrollment,
            LocalDate reportDate,
            AttendanceRecord attendance,
            List<CareRecord> careRecords) {
        String attendanceText = attendance != null ? attendance.getStatus().getDescription() : "暂无考勤";
        return enrollment.getBaby().getName() + " " + reportDate + " 日报："
                + attendanceText + "，照护记录 " + careRecords.size() + " 条。";
    }

    private String buildAttendanceSummary(AttendanceRecord attendance) {
        if (attendance == null) {
            return "暂无考勤记录";
        }
        StringBuilder builder = new StringBuilder(attendance.getStatus().getDescription());
        if (attendance.getCheckInAt() != null) {
            builder.append("，到园 ").append(attendance.getCheckInAt().toLocalTime());
        }
        if (attendance.getCheckOutAt() != null) {
            builder.append("，离园 ").append(attendance.getCheckOutAt().toLocalTime());
        }
        if (attendance.getTemperature() != null) {
            builder.append("，体温 ").append(attendance.getTemperature()).append("℃");
        }
        return builder.toString();
    }

    private String buildCareSummary(List<CareRecord> careRecords) {
        if (careRecords.isEmpty()) {
            return "暂无照护记录";
        }
        Map<CareRecord.CareType, Integer> counts = new EnumMap<>(CareRecord.CareType.class);
        careRecords.forEach(record -> counts.merge(record.getType(), 1, Integer::sum));
        return counts.entrySet().stream()
                .map(entry -> entry.getKey().getDescription() + entry.getValue() + "条")
                .collect(Collectors.joining("，"));
    }

    private String buildHealthSummary(List<CareRecord> careRecords, List<HealthObservation> healthObservations) {
        List<String> temperatures = careRecords.stream()
                .filter(record -> record.getType() == CareRecord.CareType.TEMPERATURE)
                .map(record -> formatCareValue(record, "℃"))
                .collect(Collectors.toList());
        long abnormalCount = healthObservations.stream()
                .filter(observation -> Boolean.TRUE.equals(observation.getAbnormal()))
                .count();
        String temperatureText = temperatures.isEmpty() ? "暂无体温记录" : "体温：" + String.join("，", temperatures);
        if (healthObservations.isEmpty()) {
            return temperatureText;
        }
        return temperatureText + "；健康观察" + healthObservations.size() + "条，异常" + abnormalCount + "条";
    }

    private String buildActivitySummary(List<CareRecord> careRecords) {
        List<String> activities = careRecords.stream()
                .filter(record -> record.getType() == CareRecord.CareType.ACTIVITY)
                .map(record -> StringUtils.hasText(record.getValueText()) ? record.getValueText() : "活动")
                .collect(Collectors.toList());
        return activities.isEmpty() ? "暂无活动记录" : "活动：" + String.join("，", activities);
    }

    private String formatCareValue(CareRecord record, String defaultUnit) {
        if (record.getAmount() != null) {
            return record.getAmount() + (StringUtils.hasText(record.getUnit()) ? record.getUnit() : defaultUnit);
        }
        return StringUtils.hasText(record.getValueText()) ? record.getValueText() : "已记录";
    }

    private String buildAiDraftContent(
            Enrollment enrollment,
            LocalDate reportDate,
            AttendanceRecord attendance,
            List<CareRecord> careRecords,
            List<HealthObservation> healthObservations) {
        StringBuilder builder = new StringBuilder();
        builder.append(enrollment.getBaby().getName())
                .append(" ")
                .append(reportDate)
                .append(" 在园日报草稿：\n");
        builder.append("1. 出勤情况：").append(buildAttendanceSummary(attendance)).append("。\n");
        builder.append("2. 照护记录：").append(buildCareSummary(careRecords)).append("。\n");
        builder.append("3. 健康观察：").append(buildHealthSummary(careRecords, healthObservations)).append("。\n");
        builder.append("4. 活动情况：").append(buildActivitySummary(careRecords)).append("。\n");
        builder.append("5. 老师可补充：请结合孩子当天情绪、同伴互动和个别照护情况补充个性化评语。");
        return trimToLimit(builder.toString(), 1200);
    }

    private String trimToLimit(String value, int limit) {
        if (value == null || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit - 3) + "...";
    }

    private DailyReport getOwnedReport(User user, Long reportId) {
        DailyReport report = dailyReportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException("日报不存在"));
        if (!canAccessOrganization(user, report.getEnrollment().getOrganization().getId())) {
            throw new BusinessException("您无权操作该日报");
        }
        return report;
    }

    private Enrollment getOwnedEnrollment(User user, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new BusinessException("入托档案不存在"));
        if (!canAccessOrganization(user, enrollment.getOrganization().getId())) {
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
            throw new BusinessException("您无权访问该宝宝日报");
        }
        return baby;
    }

    private void assertReportVisible(User user, DailyReport report) {
        if (canAccessOrganization(user, report.getEnrollment().getOrganization().getId())) {
            return;
        }
        if (report.getStatus() != DailyReport.ReportStatus.PUBLISHED) {
            throw new BusinessException("日报尚未发布");
        }
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

    private DailyReportResponse convert(DailyReport report) {
        DailyReportResponse response = new DailyReportResponse();
        response.setId(report.getId());
        response.setEnrollmentId(report.getEnrollment().getId());
        response.setBabyId(report.getEnrollment().getBaby().getId());
        response.setBabyName(report.getEnrollment().getBaby().getName());
        response.setClassroomId(report.getEnrollment().getClassroom().getId());
        response.setClassroomName(report.getEnrollment().getClassroom().getName());
        response.setOrganizationId(report.getEnrollment().getOrganization().getId());
        response.setOrganizationName(report.getEnrollment().getOrganization().getName());
        response.setReportDate(report.getReportDate());
        response.setStatus(report.getStatus());
        response.setStatusDescription(report.getStatus().getDescription());
        response.setSummary(report.getSummary());
        response.setAttendanceSummary(report.getAttendanceSummary());
        response.setCareSummary(report.getCareSummary());
        response.setHealthSummary(report.getHealthSummary());
        response.setActivitySummary(report.getActivitySummary());
        response.setTeacherComment(report.getTeacherComment());
        response.setAiDraftContent(report.getAiDraftContent());
        response.setPublishedAt(report.getPublishedAt());
        if (report.getPublishedBy() != null) {
            response.setPublishedById(report.getPublishedBy().getId());
            response.setPublishedByName(report.getPublishedBy().getNickname());
        }
        response.setCreatedAt(report.getCreatedAt());
        response.setUpdatedAt(report.getUpdatedAt());
        return response;
    }
}
