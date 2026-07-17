package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BillingStatementCreateRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    private Long feeItemId;

    @Size(max = 120, message = "账单标题不能超过120个字符")
    private String title;

    private BigDecimal amount;

    private LocalDate dueDate;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String remark;
}
