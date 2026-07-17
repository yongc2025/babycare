package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class FeeItemRequest {

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    @NotBlank(message = "收费项目名称不能为空")
    @Size(max = 80, message = "收费项目名称不能超过80个字符")
    private String name;

    @Size(max = 300, message = "收费项目说明不能超过300个字符")
    private String description;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private String status;
}
