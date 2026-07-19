package com.huigrowth.babycare.dto;

import com.huigrowth.babycare.entity.SafetyLedger;
import com.huigrowth.babycare.entity.SafetyLedgerTemplate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SafetyLedgerTemplateRequest {
    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    @NotNull(message = "台账类型不能为空")
    private String ledgerType;

    @NotNull(message = "频率不能为空")
    private String frequency;

    private Integer dayOfWeek;

    private Integer dayOfMonth;

    @NotBlank(message = "模板标题不能为空")
    private String title;

    private String location;

    private String responsiblePerson;

    private String content;

    private Boolean isActive;

    private LocalDate nextGenerateDate;
}
