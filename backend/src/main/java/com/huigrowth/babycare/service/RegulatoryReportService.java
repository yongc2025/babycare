package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.RegulatoryExportRow;
import com.huigrowth.babycare.dto.RegulatoryReportResponse;
import com.huigrowth.babycare.entity.AttendanceRecord;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.HealthObservation;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.SafetyLedger;
import com.huigrowth.babycare.entity.Staff;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.AttendanceRecordRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.HealthObservationRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.SafetyLedgerRepository;
import com.huigrowth.babycare.repository.StaffRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RegulatoryReportService {

    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final StaffRepository staffRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final HealthObservationRepository healthObservationRepository;
    private final SafetyLedgerRepository safetyLedgerRepository;
    private final UserRepository userRepository;

    public RegulatoryReportResponse getOrganizationReport(
            String username,
            Long organizationId,
            LocalDate startDate,
            LocalDate endDate) {
        User operator = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        if (!organizationRepository.existsByIdAndCreatedBy(organizationId, operator)) {
            throw new BusinessException("您无权访问该机构监管报表");
        }

        LocalDate periodEnd = endDate != null ? endDate : LocalDate.now();
        LocalDate periodStart = startDate != null ? startDate : periodEnd.withDayOfMonth(1);
        if (periodStart.isAfter(periodEnd)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (ChronoUnit.DAYS.between(periodStart, periodEnd) > 366) {
            throw new BusinessException("监管报表时间范围不能超过366天");
        }

        List<Classroom> classrooms = classroomRepository.findByOrganizationOrderByCreatedAtDesc(organization);
        List<Enrollment> enrollments = enrollmentRepository.findByOrganizationOrderByCreatedAtDesc(organization);
        List<Staff> staffList = staffRepository.findByOrganizationOrderByCreatedAtDesc(organization);
        List<SafetyLedger> safetyLedgers = safetyLedgerRepository
                .findByOrganizationAndLedgerDateBetweenOrderByLedgerDateDescCreatedAtDesc(
                        organization,
                        periodStart,
                        periodEnd);

        RegulatoryReportResponse response = new RegulatoryReportResponse();
        fillOrganizationFields(response, organization, periodStart, periodEnd);
        fillClassroomAndEnrollmentFields(response, classrooms, enrollments);
        fillStaffFields(response, staffList);
        fillAttendanceAndHealthFields(response, classrooms, periodStart, periodEnd);
        fillSafetyFields(response, safetyLedgers);
        fillMissingFields(response);
        fillExportRows(response);
        return response;
    }

    private void fillOrganizationFields(
            RegulatoryReportResponse response,
            Organization organization,
            LocalDate periodStart,
            LocalDate periodEnd) {
        response.setOrganizationId(organization.getId());
        response.setOrganizationName(organization.getName());
        response.setRegistrationNo(organization.getRegistrationNo());
        response.setLicenseNo(organization.getLicenseNo());
        response.setLegalRepresentative(organization.getLegalRepresentative());
        response.setSupervisorDepartment(organization.getSupervisorDepartment());
        response.setOrganizationLevel(organization.getOrganizationLevel());
        response.setOperationType(organization.getOperationType());
        response.setPeriodStart(periodStart);
        response.setPeriodEnd(periodEnd);
    }

    private void fillClassroomAndEnrollmentFields(
            RegulatoryReportResponse response,
            List<Classroom> classrooms,
            List<Enrollment> enrollments) {
        int totalCapacity = classrooms.stream()
                .map(Classroom::getCapacity)
                .filter(capacity -> capacity != null)
                .reduce(0, Integer::sum);
        int activeEnrollmentCount = (int) enrollments.stream()
                .filter(this::isActiveEnrollment)
                .count();

        response.setClassroomCount(classrooms.size());
        response.setTotalCapacity(totalCapacity);
        response.setActiveEnrollmentCount(activeEnrollmentCount);
        response.setCapacityUsageRate(totalCapacity == 0 ? 0D : activeEnrollmentCount * 100D / totalCapacity);
    }

    private void fillStaffFields(RegulatoryReportResponse response, List<Staff> staffList) {
        response.setStaffCount(staffList.size());
        response.setDirectorCount(countStaffByRole(staffList, Staff.StaffRole.DIRECTOR));
        response.setTeacherCount(countStaffByRole(staffList, Staff.StaffRole.TEACHER));
        response.setCaregiverCount(countStaffByRole(staffList, Staff.StaffRole.CAREGIVER));
        response.setFinanceCount(countStaffByRole(staffList, Staff.StaffRole.FINANCE));
    }

    private void fillAttendanceAndHealthFields(
            RegulatoryReportResponse response,
            List<Classroom> classrooms,
            LocalDate periodStart,
            LocalDate periodEnd) {
        int attendanceRecordCount = 0;
        int leaveRecordCount = 0;
        int healthObservationCount = 0;
        int abnormalObservationCount = 0;
        int followUpObservationCount = 0;

        for (LocalDate date = periodStart; !date.isAfter(periodEnd); date = date.plusDays(1)) {
            for (Classroom classroom : classrooms) {
                List<AttendanceRecord> attendanceRecords = attendanceRecordRepository
                        .findByEnrollmentClassroomAndAttendanceDateOrderByCreatedAtDesc(classroom, date);
                attendanceRecordCount += attendanceRecords.size();
                leaveRecordCount += (int) attendanceRecords.stream()
                        .filter(record -> record.getStatus() == AttendanceRecord.AttendanceStatus.LEAVE)
                        .count();

                List<HealthObservation> observations = healthObservationRepository
                        .findByEnrollmentClassroomAndObservationDateOrderByObservationTimeDesc(classroom, date);
                healthObservationCount += observations.size();
                abnormalObservationCount += (int) observations.stream()
                        .filter(observation -> Boolean.TRUE.equals(observation.getAbnormal()))
                        .count();
                followUpObservationCount += (int) observations.stream()
                        .filter(observation -> Boolean.TRUE.equals(observation.getFollowUpRequired()))
                        .count();
            }
        }

        response.setAttendanceRecordCount(attendanceRecordCount);
        response.setLeaveRecordCount(leaveRecordCount);
        response.setHealthObservationCount(healthObservationCount);
        response.setAbnormalObservationCount(abnormalObservationCount);
        response.setFollowUpObservationCount(followUpObservationCount);
    }

    private void fillSafetyFields(RegulatoryReportResponse response, List<SafetyLedger> safetyLedgers) {
        response.setSafetyLedgerCount(safetyLedgers.size());
        response.setOpenSafetyLedgerCount(countSafetyByStatus(safetyLedgers, SafetyLedger.LedgerStatus.OPEN));
        response.setClosedSafetyLedgerCount(countSafetyByStatus(safetyLedgers, SafetyLedger.LedgerStatus.CLOSED));
        response.setOverdueSafetyLedgerCount(countSafetyByStatus(safetyLedgers, SafetyLedger.LedgerStatus.OVERDUE));
    }

    private void fillMissingFields(RegulatoryReportResponse response) {
        addMissing(response, response.getRegistrationNo(), "备案编号");
        addMissing(response, response.getLicenseNo(), "办学/托育许可证号");
        addMissing(response, response.getLegalRepresentative(), "法定代表人");
        addMissing(response, response.getSupervisorDepartment(), "主管部门");
        addMissing(response, response.getOrganizationLevel(), "机构等级");
        addMissing(response, response.getOperationType(), "运营类型");
    }

    private void fillExportRows(RegulatoryReportResponse response) {
        addRow(response, "机构备案", "机构名称", "organizationName", response.getOrganizationName());
        addRow(response, "机构备案", "备案编号", "registrationNo", response.getRegistrationNo());
        addRow(response, "机构备案", "办学/托育许可证号", "licenseNo", response.getLicenseNo());
        addRow(response, "机构备案", "法定代表人", "legalRepresentative", response.getLegalRepresentative());
        addRow(response, "机构备案", "主管部门", "supervisorDepartment", response.getSupervisorDepartment());
        addRow(response, "机构备案", "机构等级", "organizationLevel", response.getOrganizationLevel());
        addRow(response, "机构备案", "运营类型", "operationType", response.getOperationType());
        addRow(response, "运营规模", "班级数量", "classroomCount", response.getClassroomCount());
        addRow(response, "运营规模", "托位容量", "totalCapacity", response.getTotalCapacity());
        addRow(response, "运营规模", "在托幼儿数", "activeEnrollmentCount", response.getActiveEnrollmentCount());
        addRow(response, "人员配置", "员工总数", "staffCount", response.getStaffCount());
        addRow(response, "人员配置", "教师数", "teacherCount", response.getTeacherCount());
        addRow(response, "人员配置", "保育员数", "caregiverCount", response.getCaregiverCount());
        addRow(response, "健康保健", "健康观察记录数", "healthObservationCount", response.getHealthObservationCount());
        addRow(response, "健康保健", "异常观察数", "abnormalObservationCount", response.getAbnormalObservationCount());
        addRow(response, "安全卫生", "安全卫生台账数", "safetyLedgerCount", response.getSafetyLedgerCount());
        addRow(response, "安全卫生", "逾期台账数", "overdueSafetyLedgerCount", response.getOverdueSafetyLedgerCount());
    }

    private int countStaffByRole(List<Staff> staffList, Staff.StaffRole role) {
        return (int) staffList.stream().filter(staff -> staff.getRole() == role).count();
    }

    private int countSafetyByStatus(List<SafetyLedger> safetyLedgers, SafetyLedger.LedgerStatus status) {
        return (int) safetyLedgers.stream().filter(ledger -> ledger.getStatus() == status).count();
    }

    private boolean isActiveEnrollment(Enrollment enrollment) {
        return enrollment.getStatus() == Enrollment.EnrollmentStatus.ACTIVE
                || enrollment.getStatus() == Enrollment.EnrollmentStatus.PENDING;
    }

    private void addMissing(RegulatoryReportResponse response, String value, String fieldName) {
        if (!StringUtils.hasText(value)) {
            response.getMissingRegulatoryFields().add(fieldName);
        }
    }

    private void addRow(
            RegulatoryReportResponse response,
            String category,
            String fieldName,
            String fieldCode,
            Object value) {
        String text = value == null ? "" : String.valueOf(value);
        String status = StringUtils.hasText(text) ? "READY" : "MISSING";
        response.getExportRows().add(new RegulatoryExportRow(category, fieldName, fieldCode, text, status));
    }
}
