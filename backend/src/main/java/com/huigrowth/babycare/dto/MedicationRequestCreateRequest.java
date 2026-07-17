package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MedicationRequestCreateRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    @NotBlank(message = "药品名称不能为空")
    @Size(max = 80, message = "药品名称不能超过80个字符")
    private String medicineName;

    @Size(max = 80, message = "剂量不能超过80个字符")
    private String dosage;

    @Size(max = 120, message = "频次不能超过120个字符")
    private String frequency;

    @NotNull(message = "开始日期不能为空")
    private LocalDate startDate;

    @NotNull(message = "结束日期不能为空")
    private LocalDate endDate;

    @Size(max = 300, message = "用药说明不能超过300个字符")
    private String instructions;
}
