package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.BossDashboardResponse;
import com.huigrowth.babycare.entity.*;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 老板/机构管理员多园区驾驶舱服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BossDashboardService {

    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final BillingStatementRepository billingStatementRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;

    public BossDashboardResponse getOverview(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 获取用户管理的所有机构（通过 Staff 关联或创建的机构）
        List<Organization> orgs = organizationRepository.findByStaffUser(user);
        if (orgs.isEmpty()) {
            // 如果未通过 Staff 关联，则用创建的机构
            orgs = organizationRepository.findByCreatedByOrderByCreatedAtDesc(user);
        }

        LocalDate today = LocalDate.now();
        BossDashboardResponse resp = new BossDashboardResponse();
        resp.setTotalOrganizations(orgs.size());

        int totalClassrooms = 0;
        int totalEnrollments = 0;
        int totalCheckedIn = 0;
        int totalLeave = 0;
        int totalOpenIncidents = 0;
        int totalUnpaidBills = 0;
        BigDecimal totalUnpaidAmount = BigDecimal.ZERO;
        List<BossDashboardResponse.OrgSummary> summaries = new ArrayList<>();

        for (Organization org : orgs) {
            List<Classroom> classrooms = classroomRepository.findByOrganizationOrderByCreatedAtDesc(org);
            List<Enrollment> enrollments = enrollmentRepository.findByOrganizationOrderByCreatedAtDesc(org);
            List<BillingStatement> bills = billingStatementRepository.findByOrganizationOrderByCreatedAtDesc(org);

            int activeEnrollments = (int) enrollments.stream()
                    .filter(e -> e.getStatus() == Enrollment.EnrollmentStatus.ACTIVE)
                    .count();
            int checkedIn = 0;
            int leave = 0;
            int openIncidents = 0;

            for (Classroom c : classrooms) {
                List<AttendanceRecord> attRecords = attendanceRecordRepository
                        .findByEnrollmentClassroomAndAttendanceDateOrderByCreatedAtDesc(c, today);
                checkedIn += (int) attRecords.stream()
                        .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.CHECKED_IN
                                || r.getStatus() == AttendanceRecord.AttendanceStatus.CHECKED_OUT)
                        .count();
                leave += (int) attRecords.stream()
                        .filter(r -> r.getStatus() == AttendanceRecord.AttendanceStatus.LEAVE)
                        .count();
                openIncidents += incidentReportRepository
                        .findByEnrollmentClassroomAndStatusOrderByOccurredAtDesc(c, IncidentReport.IncidentStatus.OPEN)
                        .size();
            }

            int unpaidCount = (int) bills.stream()
                    .filter(b -> b.getStatus() == BillingStatement.BillingStatus.UNPAID)
                    .count();
            BigDecimal unpaidAmt = bills.stream()
                    .filter(b -> b.getStatus() == BillingStatement.BillingStatus.UNPAID)
                    .map(BillingStatement::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // 获取园长姓名
            List<Staff> directors = staffRepository.findByOrganizationAndRoleAndStatus(org, Staff.StaffRole.DIRECTOR, Staff.StaffStatus.ACTIVE);
            String directorName = directors.isEmpty() ? "" : directors.get(0).getUser().getNickname();

            totalClassrooms += classrooms.size();
            totalEnrollments += activeEnrollments;
            totalCheckedIn += checkedIn;
            totalLeave += leave;
            totalOpenIncidents += openIncidents;
            totalUnpaidBills += unpaidCount;
            totalUnpaidAmount = totalUnpaidAmount.add(unpaidAmt);

            BossDashboardResponse.OrgSummary summary = new BossDashboardResponse.OrgSummary();
            summary.setOrganizationId(org.getId());
            summary.setOrganizationName(org.getName());
            summary.setClassroomCount(classrooms.size());
            summary.setActiveEnrollmentCount(activeEnrollments);
            summary.setCheckedInCount(checkedIn);
            summary.setLeaveCount(leave);
            summary.setAttendanceRate(activeEnrollments == 0 ? 0D : checkedIn * 100D / activeEnrollments);
            summary.setOpenIncidentCount(openIncidents);
            summary.setDirectorName(directorName);
            summaries.add(summary);
        }

        resp.setTotalClassrooms(totalClassrooms);
        resp.setTotalEnrollments(totalEnrollments);
        resp.setTotalCheckedInToday(totalCheckedIn);
        resp.setTotalLeaveToday(totalLeave);
        resp.setOverallAttendanceRate(totalEnrollments == 0 ? 0D : totalCheckedIn * 100D / totalEnrollments);
        resp.setTotalOpenIncidents(totalOpenIncidents);
        resp.setTotalUnpaidBills(totalUnpaidBills);
        resp.setTotalUnpaidAmount(totalUnpaidAmount);
        resp.setOrgSummaries(summaries);

        return resp;
    }
}
