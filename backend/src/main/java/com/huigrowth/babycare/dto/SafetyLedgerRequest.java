package com.huigrowth.babycare.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class SafetyLedgerRequest {

    @NotNull(message = "机构ID不能为空")
    private Long organizationId;

    private Long relatedIncidentId;

    @NotNull(message = "台账日期不能为空")
    private LocalDate ledgerDate;

    @NotBlank(message = "台账类型不能为空")
    private String ledgerType;

    @NotBlank(message = "台账标题不能为空")
    private String title;

    private String content;
    private String location;
    private String responsiblePerson;
    private LocalDateTime dueAt;
    private String status;
    private String handleRemark;
}
