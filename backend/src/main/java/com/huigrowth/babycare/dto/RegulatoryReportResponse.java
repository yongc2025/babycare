package com.huigrowth.babycare.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class RegulatoryReportResponse {
    private Long organizationId;
    private String organizationName;
    private String registrationNo;
    private String licenseNo;
    private String legalRepresentative;
    private String supervisorDepartment;
    private String organizationLevel;
    private String operationType;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer classroomCount;
    private Integer totalCapacity;
    private Integer activeEnrollmentCount;
    private Double capacityUsageRate;
    private Integer staffCount;
    private Integer directorCount;
    private Integer teacherCount;
    private Integer caregiverCount;
    private Integer financeCount;
    private Integer attendanceRecordCount;
    private Integer leaveRecordCount;
    private Integer healthObservationCount;
    private Integer abnormalObservationCount;
    private Integer followUpObservationCount;
    private Integer safetyLedgerCount;
    private Integer openSafetyLedgerCount;
    private Integer closedSafetyLedgerCount;
    private Integer overdueSafetyLedgerCount;
    private List<String> missingRegulatoryFields = new ArrayList<>();
    private List<RegulatoryExportRow> exportRows = new ArrayList<>();
}
