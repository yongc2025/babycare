package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AdmissionTrialRequest {

    @NotNull(message = "试托开始日期不能为空")
    private LocalDate trialStartDate;

    @NotNull(message = "试托结束日期不能为空")
    private LocalDate trialEndDate;

    private String trialFeedback;
}
