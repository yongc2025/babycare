package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MedicationReviewRequest {

    @Size(max = 300, message = "审核备注不能超过300个字符")
    private String reviewRemark;
}
