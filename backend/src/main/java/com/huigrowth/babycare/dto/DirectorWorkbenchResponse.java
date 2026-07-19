package com.huigrowth.babycare.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 园长工作台响应：含概览指标、待办事项、风险预警
 */
@Data
public class DirectorWorkbenchResponse {
    private Long organizationId;
    private String organizationName;
    private LocalDate date;

    // 概览指标
    private Integer classroomCount;
    private Integer activeEnrollmentCount;
    private Integer checkedInCount;
    private Integer leaveCount;
    private Double attendanceRate;
    private Integer openIncidentCount;
    private Integer unpaidBillCount;
    private BigDecimal unpaidBillAmount;

    // 待办事项
    private List<TodoItem> pendingTodos;

    // 风险预警
    private List<RiskAlert> riskAlerts;

    @Data
    public static class TodoItem {
        private Long id;
        private String type;        // LEAVE_APPROVAL, INCIDENT_HANDLE, BILL_REMIND, ENROLLMENT_REVIEW
        private String typeName;    // 请假审批, 事件处理, 催缴账单, 入托审核
        private String title;
        private String description;
        private String status;
        private LocalDateTime createdAt;
    }

    @Data
    public static class RiskAlert {
        private Long id;
        private String type;        // HIGH_ABSENTEEISM, OPEN_INCIDENT, UNPAID_BILLS, LOW_ATTENDANCE
        private String typeName;    // 缺勤偏高, 待处理事件, 欠费偏高, 出勤率偏低
        private String title;
        private String description;
        private String severity;    // HIGH, MEDIUM, LOW
    }
}
