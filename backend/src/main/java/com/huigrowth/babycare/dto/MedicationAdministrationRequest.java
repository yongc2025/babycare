package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicationAdministrationRequest {

    @NotNull(message = "用药委托ID不能为空")
    private Long medicationRequestId;

    private LocalDateTime administeredAt;

    @Size(max = 80, message = "实际剂量不能超过80个字符")
    private String actualDosage;

    private Boolean reactionObserved;

    @Size(max = 300, message = "执行备注不能超过300个字符")
    private String remark;
}
