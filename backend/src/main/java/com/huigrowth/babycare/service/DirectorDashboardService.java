package com.huigrowth.babycare.service;

import com.huigrowth.babycare.dto.DirectorDashboardResponse;
import com.huigrowth.babycare.dto.DirectorWorkbenchResponse;
import com.huigrowth.babycare.entity.*;
import com.huigrowth.babycare.exception.BusinessException;
import com.huigrowth.babycare.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    private final LeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    public DirectorDashboardResponse getOverview(String username, Long organizationId, LocalDate date) {
        User operator = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));

        // 数据范围校验：创建者、园长或有 DIRECTOR 岗位的员工可访问
        boolean isCreator = organizationRepository.existsByIdAndCreatedBy(organizationId, operator);
        boolean isDirector = staffRepository.findByOrganizationAndRoleAndStatus(
                organization, Staff.StaffRole.DIRECTOR, Staff.StaffStatus.ACTIVE)
                .stream().anyMatch(s -> s.getUser().getId().equals(operator.getId()));
        if (!isCreator && !isDirector) {
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

    /**
     * 获取园长工作台数据：含概览、待办事项和风险预警
     */
    public DirectorWorkbenchResponse getWorkbench(String username, Long organizationId) {
        // 复用概览数据
        DirectorDashboardResponse overview = getOverview(username, organizationId, null);
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new BusinessException("机构不存在"));

        DirectorWorkbenchResponse workbench = new DirectorWorkbenchResponse();
        workbench.setOrganizationId(overview.getOrganizationId());
        workbench.setOrganizationName(overview.getOrganizationName());
        workbench.setDate(overview.getDate());
        workbench.setClassroomCount(overview.getClassroomCount());
        workbench.setActiveEnrollmentCount(overview.getActiveEnrollmentCount());
        workbench.setCheckedInCount(overview.getCheckedInCount());
        workbench.setLeaveCount(overview.getLeaveCount());
        workbench.setAttendanceRate(overview.getAttendanceRate());
        workbench.setOpenIncidentCount(overview.getOpenIncidentCount());
        workbench.setUnpaidBillCount(overview.getUnpaidBillCount());
        workbench.setUnpaidBillAmount(overview.getUnpaidBillAmount());

        // 待办事项
        List<DirectorWorkbenchResponse.TodoItem> todos = new ArrayList<>();
        LocalDate today = LocalDate.now();

        // 待审批请假
        List<Classroom> classrooms = classroomRepository.findByOrganizationOrderByCreatedAtDesc(organization);
        for (Classroom c : classrooms) {
            List<LeaveRequest> leaves = leaveRequestRepository.findByEnrollmentClassroomOrderByCreatedAtDesc(c);
            for (LeaveRequest lr : leaves) {
                if (lr.getStatus() == LeaveRequest.LeaveStatus.PENDING) {
                    DirectorWorkbenchResponse.TodoItem item = new DirectorWorkbenchResponse.TodoItem();
                    item.setId(lr.getId());
                    item.setType("LEAVE_APPROVAL");
                    item.setTypeName("请假审批");
                    item.setTitle("请假申请");
                    item.setDescription(lr.getReason());
                    item.setStatus(lr.getStatus().name());
                    item.setCreatedAt(lr.getCreatedAt());
                    todos.add(item);
                }
            }
        }

        // 待处理事件
        for (Classroom c : classrooms) {
            List<IncidentReport> incidents = incidentReportRepository
                    .findByEnrollmentClassroomAndStatusOrderByOccurredAtDesc(c, IncidentReport.IncidentStatus.OPEN);
            for (IncidentReport ir : incidents) {
                DirectorWorkbenchResponse.TodoItem item = new DirectorWorkbenchResponse.TodoItem();
                item.setId(ir.getId());
                item.setType("INCIDENT_HANDLE");
                item.setTypeName("事件处理");
                item.setTitle(ir.getTitle());
                item.setDescription(ir.getDescription());
                item.setStatus(ir.getStatus().name());
                item.setCreatedAt(ir.getOccurredAt() != null ? ir.getOccurredAt() : ir.getCreatedAt());
                todos.add(item);
            }
        }

        workbench.setPendingTodos(todos);

        // 风险预警
        List<DirectorWorkbenchResponse.RiskAlert> alerts = new ArrayList<>();

        // 出勤率预警（低于70%）
        double rate = overview.getAttendanceRate() != null ? overview.getAttendanceRate() : 0;
        if (rate < 70 && overview.getActiveEnrollmentCount() > 0) {
            DirectorWorkbenchResponse.RiskAlert alert = new DirectorWorkbenchResponse.RiskAlert();
            alert.setType("LOW_ATTENDANCE");
            alert.setTypeName("出勤率偏低");
            alert.setTitle("今日出勤率仅 " + String.format("%.1f", rate) + "%");
            alert.setDescription("在托 " + overview.getActiveEnrollmentCount() + " 人，到园 " + overview.getCheckedInCount() + " 人，缺勤 " + overview.getLeaveCount() + " 人");
            alert.setSeverity(rate < 50 ? "HIGH" : "MEDIUM");
            alerts.add(alert);
        }

        // 待处理事件预警
        if (overview.getOpenIncidentCount() > 0) {
            DirectorWorkbenchResponse.RiskAlert alert = new DirectorWorkbenchResponse.RiskAlert();
            alert.setType("OPEN_INCIDENT");
            alert.setTypeName("待处理事件");
            alert.setTitle(overview.getOpenIncidentCount() + " 起事件待处理");
            alert.setDescription("请及时跟进处理");
            alert.setSeverity(overview.getOpenIncidentCount() > 3 ? "HIGH" : "MEDIUM");
            alerts.add(alert);
        }

        workbench.setRiskAlerts(alerts);
        return workbench;
    }
}
