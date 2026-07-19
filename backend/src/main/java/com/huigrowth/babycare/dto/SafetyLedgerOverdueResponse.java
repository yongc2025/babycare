package com.huigrowth.babycare.dto;

import lombok.Data;

@Data
public class SafetyLedgerOverdueResponse {
    private int overdueCount;
    private int openCount;
    private int processingCount;
}
