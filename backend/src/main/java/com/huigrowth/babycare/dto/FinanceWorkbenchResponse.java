package com.huigrowth.babycare.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FinanceWorkbenchResponse {
    // 账单统计
    private long totalBillCount;
    private long paidBillCount;
    private long unpaidBillCount;
    private long overdueBillCount;
    private BigDecimal totalRevenue;
    private BigDecimal unpaidAmount;
    // 招生转化
    private long totalLeadCount;
    private long newLeadCount;
    private long followingLeadCount;
    private long appliedLeadCount;
    private long approvedLeadCount;
    private long trialingLeadCount;
    private long enrolledLeadCount;
    private long lostLeadCount;
    // 收费项目
    private long activeFeeItemCount;
}
