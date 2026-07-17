package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BillingPaymentRequest {

    @Size(max = 30, message = "支付方式不能超过30个字符")
    private String paymentMethod;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String remark;
}
