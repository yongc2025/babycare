package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdmissionReviewRequest {

    @NotBlank(message = "审核结果不能为空")
    private String result;

    private String reviewRemark;
}
