package com.huigrowth.babycare.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class DirectorDashboardResponse {
    private Long organizationId;
    private String organizationName;
    private LocalDate date;
    private Integer classroomCount;
    private Integer activeEnrollmentCount;
    private Integer expectedAttendanceCount;
    private Integer checkedInCount;
    private Integer leaveCount;
    private Double attendanceRate;
    private Integer openIncidentCount;
    private Integer unpaidBillCount;
    private BigDecimal unpaidBillAmount;
    private Integer publishedAnnouncementCount;
}
