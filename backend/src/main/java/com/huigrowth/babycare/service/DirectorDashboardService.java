package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.DirectorDashboardResponse;
import com.huigrowth.babycare.entity.Announcement;
import com.huigrowth.babycare.entity.AttendanceRecord;
import com.huigrowth.babycare.entity.BillingStatement;
import com.huigrowth.babycare.entity.Classroom;
import com.huigrowth.babycare.entity.Enrollment;
import com.huigrowth.babycare.entity.IncidentReport;
import com.huigrowth.babycare.entity.Organization;
import com.huigrowth.babycare.entity.User;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.AnnouncementRepository;
import com.huigrowth.babycare.repository.AttendanceRecordRepository;
import com.huigrowth.babycare.repository.BillingStatementRepository;
import com.huigrowth.babycare.repository.ClassroomRepository;
import com.huigrowth.babycare.repository.EnrollmentRepository;
import com.huigrowth.babycare.repository.IncidentReportRepository;
import com.huigrowth.babycare.repository.OrganizationRepository;
import com.huigrowth.babycare.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DirectorDashboardService {

    private final OrganizationRepository organizationRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final IncidentReportRepository incidentReportRepository;
    private final BillingStatementRepository billingStatementRepository;
    private final AnnouncementRepository announcementRepository;
    private final UserRepository userRepository;

    public DirectorDashboardResponse getOverview(String username, Long organizationId, LocalDate date) {
        User operator = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));
        if (!organizationRepository.existsByIdAndCreatedBy(organizationId, operator)) {
            throw new BusinessException("您无权访问该机构驾驶舱");
        }

        LocalDate targetDate = date != null ? date : LocalDate.now();
        List<Classroom> classrooms = classroomRepository.findByOrganizationOrderByCreatedAtDesc(organization);
        List<Enrollment> enrollments = enrollmentRepository.findByOrganizationOrderByCreatedAtDesc(organization);
        List<BillingStatement> bills = billingStatementRepository.findByOrganizationOrderByCreatedAtDesc(organization);
        List<Announcement> announcements = announcementRepository.findByOrganizationOrderByCreatedAtDesc(organization);

        int activeEnrollments = (int) enrollments.stream()
                .filter(enrollment -> enrollment.getStatus() == Enrollment.EnrollmentStatus.ACTIVE)
                .count();
        int checkedIn = 0;
        int leave = 0;
        int openIncidents = 0;
        for (Classroom classroom : classrooms) {
            List<AttendanceRecord> attendance = attendanceRecordRepository
                    .findByEnrollmentClassroomAndAttendanceDateOrderByCreatedAtDesc(classroom, targetDate);
            checkedIn += (int) attendance.stream()
                    .filter(record -> record.getStatus() == AttendanceRecord.AttendanceStatus.CHECKED_IN
                            || record.getStatus() == AttendanceRecord.AttendanceStatus.CHECKED_OUT)
                    .count();
            leave += (int) attendance.stream()
                    .filter(record -> record.getStatus() == AttendanceRecord.AttendanceStatus.LEAVE)
                    .count();
            openIncidents += incidentReportRepository
                    .findByEnrollmentClassroomAndStatusOrderByOccurredAtDesc(
                            classroom,
                            IncidentReport.IncidentStatus.OPEN)
                    .size();
        }

        int unpaidBillCount = (int) bills.stream()
                .filter(bill -> bill.getStatus() == BillingStatement.BillingStatus.UNPAID)
                .count();
        BigDecimal unpaidBillAmount = bills.stream()
                .filter(bill -> bill.getStatus() == BillingStatement.BillingStatus.UNPAID)
                .map(BillingStatement::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        int publishedAnnouncements = (int) announcements.stream()
                .filter(item -> item.getStatus() == Announcement.AnnouncementStatus.PUBLISHED)
                .count();

        DirectorDashboardResponse response = new DirectorDashboardResponse();
        response.setOrganizationId(organization.getId());
        response.setOrganizationName(organization.getName());
        response.setDate(targetDate);
        response.setClassroomCount(classrooms.size());
        response.setActiveEnrollmentCount(activeEnrollments);
        response.setExpectedAttendanceCount(activeEnrollments);
        response.setCheckedInCount(checkedIn);
        response.setLeaveCount(leave);
        response.setAttendanceRate(activeEnrollments == 0 ? 0D : checkedIn * 100D / activeEnrollments);
        response.setOpenIncidentCount(openIncidents);
        response.setUnpaidBillCount(unpaidBillCount);
        response.setUnpaidBillAmount(unpaidBillAmount);
        response.setPublishedAnnouncementCount(publishedAnnouncements);
        return response;
    }
}
