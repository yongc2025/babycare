package com.huigrowth.babycare.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 老板/机构管理员多园区驾驶舱响应
 */
@Data
public class BossDashboardResponse {
    private Integer totalOrganizations;
    private Integer totalClassrooms;
    private Integer totalEnrollments;
    private Integer totalCheckedInToday;
    private Integer totalLeaveToday;
    private Double overallAttendanceRate;
    private Integer totalOpenIncidents;
    private Integer totalUnpaidBills;
    private BigDecimal totalUnpaidAmount;
    private List<OrgSummary> orgSummaries;

    @Data
    public static class OrgSummary {
        private Long organizationId;
        private String organizationName;
        private Integer classroomCount;
        private Integer activeEnrollmentCount;
        private Integer checkedInCount;
        private Integer leaveCount;
        private Double attendanceRate;
        private Integer openIncidentCount;
        private String directorName;
    }
}
