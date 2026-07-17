package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AllergyTagRequest {

    @NotNull(message = "入托档案ID不能为空")
    private Long enrollmentId;

    @NotBlank(message = "过敏源不能为空")
    @Size(max = 80, message = "过敏源不能超过80个字符")
    private String allergen;

    @Size(max = 300, message = "过敏反应不能超过300个字符")
    private String reaction;

    private String severity;

    private String status;

    @Size(max = 300, message = "备注不能超过300个字符")
    private String remark;
}
