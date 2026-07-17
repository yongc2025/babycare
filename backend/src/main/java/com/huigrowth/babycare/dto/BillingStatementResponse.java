package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.BillingStatement;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BillingStatementResponse {
    private Long id;
    private Long organizationId;
    private String organizationName;
    private Long enrollmentId;
    private Long babyId;
    private String babyName;
    private Long feeItemId;
    private String feeItemName;
    private String title;
    private BigDecimal amount;
    private LocalDate dueDate;
    private BillingStatement.BillingStatus status;
    private String statusDescription;
    private LocalDateTime paidAt;
    private String paymentMethod;
    private String remark;
    private Long createdById;
    private String createdByName;
    private Long paidById;
    private String paidByName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
